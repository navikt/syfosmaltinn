package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.network.sockets.SocketTimeoutException
import io.ktor.serialization.jackson.jackson
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.altinn.AltinnClient
import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.altinn.config.createPort
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookupFacotry
import no.nav.syfo.altinn.pdf.PdfgenClient
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.application.exception.ServiceUnavailableException
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.exception.AltinnException
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toConsumerConfig
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.kafka.NLRequestProducer
import no.nav.syfo.narmesteleder.kafka.NLResponseProducer
import no.nav.syfo.narmesteleder.kafka.NarmestelederConsumer
import no.nav.syfo.narmesteleder.kafka.model.NarmestelederLeesah
import no.nav.syfo.narmesteleder.kafka.model.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafka.model.NlResponseKafkaMessage
import no.nav.syfo.narmesteleder.kafka.utils.JacksonKafkaSerializer
import no.nav.syfo.narmesteleder.service.BeOmNyNLService
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.SendtSykmeldingService
import no.nav.syfo.sykmelding.db.Database
import no.nav.syfo.sykmelding.kafka.aiven.SendtSykmeldingAivenConsumer
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import no.nav.syfo.sykmelding.kafka.utils.JacksonKafkaDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.threeten.bp.Duration

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.syfosmaltinn")
val securelog = LoggerFactory.getLogger("securelog")

@DelicateCoroutinesApi
fun main() {
    val env = Environment()
    DefaultExports.initialize()
    val applicationState = ApplicationState()
    val applicationEngine =
        createApplicationEngine(
            env,
            applicationState,
        )
    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    val database = Database(env)

    val kafkaProducer =
        KafkaProducer<String, NlRequestKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("nl-request-producer")
                .toProducerConfig(
                    "${env.applicationName}-producer",
                    JacksonKafkaSerializer::class,
                    StringSerializer::class
                ),
        )
    val kafkaProducerNlResponse =
        KafkaProducer<String, NlResponseKafkaMessage>(
            KafkaUtils.getAivenKafkaConfig("nl-resposne-producer")
                .toProducerConfig(
                    "${env.applicationName}-producer",
                    JacksonKafkaSerializer::class,
                    StringSerializer::class
                ),
        )
    val nlRequestProducer = NLRequestProducer(kafkaProducer, env.beOmNLKafkaTopic)
    val nlResponseProducer = NLResponseProducer(kafkaProducerNlResponse, env.brytNLKafkaTopic)
    val beOmNyNLService = BeOmNyNLService(nlRequestProducer, nlResponseProducer, database)

    val iCorrespondenceAgencyExternalBasic = createPort(env.altinnUrl)
    val altinnClient =
        AltinnClient(
            username = env.altinnUsername,
            password = env.altinnPassword,
            iCorrespondenceAgencyExternalBasic = iCorrespondenceAgencyExternalBasic,
        )
    val altinnOrgnummerLookup = AltinnOrgnummerLookupFacotry.getOrgnummerResolver(env.cluster)

    val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        install(ContentNegotiation) {
            jackson {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        expectSuccess = false
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                when (exception) {
                    is SocketTimeoutException ->
                        throw ServiceUnavailableException(exception.message)
                }
            }
        }
        install(HttpRequestRetry) {
            constantDelay(50, 0, false)
            retryOnExceptionIf(3) { request, throwable ->
                log.warn("Caught exception ${throwable.message}, for url ${request.url}")
                true
            }
            retryIf(maxRetries) { request, response ->
                if (response.status.value.let { it in 500..599 }) {
                    log.warn(
                        "Retrying for statuscode ${response.status.value}, for url ${request.url}"
                    )
                    true
                } else {
                    false
                }
            }
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 20_000
            connectTimeoutMillis = 20_000
            requestTimeoutMillis = 20_000
        }
    }

    val httpClient = HttpClient(Apache, config)
    val accessTokenClient =
        AccessTokenClient(
            aadAccessTokenUrl = env.aadAccessTokenUrl,
            clientId = env.clientId,
            clientSecret = env.clientSecret,
            httpClient = httpClient,
        )
    val pdlClient =
        PdlClient(
            httpClient,
            env.pdlBasePath,
            PdlClient::class.java.getResource("/graphql/getPerson.graphql").readText(),
            accessTokenClient,
            env.pdlScope,
        )
    val narmestelederDb = NarmestelederDB(database)
    val narmesteLederService = NarmesteLederService(narmestelederDb, pdlClient)

    val retrySettings =
        RetrySettings.newBuilder()
            .setTotalTimeout(Duration.ofMillis(3000))
            .setMaxAttempts(3)
            .build()
    val juridiskloggStorage: Storage =
        StorageOptions.newBuilder().setRetrySettings(retrySettings).build().service
    val juridiskLoggService = JuridiskLoggService(env.juridiskloggBucketName, juridiskloggStorage)

    val pdfgenClient = PdfgenClient(env.pdfgenUrl, httpClient)

    val altinnSendtSykmeldingService =
        AltinnSykmeldingService(
            altinnClient,
            altinnOrgnummerLookup,
            juridiskLoggService,
            database,
            pdfgenClient,
        )

    val aivenKafkaSykmeldingConsumer: KafkaConsumer<String, SendSykmeldingAivenKafkaMessage> =
        getKafkaConsumer(env = env, resetConfig = "none")
    val aivenKafkaNarmestelederConsumer: KafkaConsumer<String, NarmestelederLeesah> =
        getKafkaConsumer(
            env = env,
            resetConfig = "none",
            consumerGroup = env.applicationName + "-nl-consumer"
        )

    val narmestelederConsumer =
        NarmestelederConsumer(
            narmestelederDb,
            aivenKafkaNarmestelederConsumer,
            env.narmestelederLeesahTopic,
            applicationState
        )

    narmestelederConsumer.startConsumer()

    val sendtSykmeldingAivenConsumer =
        SendtSykmeldingAivenConsumer(
            aivenKafkaSykmeldingConsumer,
            env.sendtSykmeldingAivenKafkaTopic
        )
    val sendtSykmeldingService =
        SendtSykmeldingService(
            applicationState,
            altinnSendtSykmeldingService,
            pdlClient,
            narmesteLederService,
            beOmNyNLService,
            sendtSykmeldingAivenConsumer,
        )

    GlobalScope.launch(Dispatchers.IO) {
        try {
            sendtSykmeldingService.start()
        } catch (e: AltinnException) {
            log.error(e.message, e.cause)
        } catch (e: Exception) {
            log.error("Noe gikk galt: ", e)
        } finally {
            applicationState.ready = false
            applicationState.alive = false
        }
    }

    applicationServer.start()
}

private inline fun <reified T : Any> getKafkaConsumer(
    env: Environment,
    resetConfig: String = "none",
    consumerGroup: String = env.applicationName + "-consumer"
) =
    KafkaConsumer(
        KafkaUtils.getAivenKafkaConfig("nl-consumer")
            .also {
                it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "100"
                it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = resetConfig
            }
            .toConsumerConfig(consumerGroup, JacksonKafkaDeserializer::class),
        StringDeserializer(),
        JacksonKafkaDeserializer(T::class),
    )
