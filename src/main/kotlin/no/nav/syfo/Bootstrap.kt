package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.altinn.AltinnClient
import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.altinn.config.createPort
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookupFacotry
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.juridisklogg.JuridiskLoggClient
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.kafka.aiven.KafkaUtils
import no.nav.syfo.kafka.toConsumerConfig
import no.nav.syfo.kafka.toProducerConfig
import no.nav.syfo.narmesteleder.client.NarmestelederClient
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
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ProxySelector

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.syfosmaltinn")

@KtorExperimentalAPI
fun main() {
    val env = Environment()
    DefaultExports.initialize()
    val applicationState = ApplicationState()
    val applicationEngine = createApplicationEngine(
        env,
        applicationState
    )
    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()
    val database = Database(env)
    val vaultSecrets = VaultSecrets()

    val kafkaProducer = KafkaProducer<String, NlRequestKafkaMessage>(
        KafkaUtils
            .getAivenKafkaConfig()
            .toProducerConfig("${env.applicationName}-producer", JacksonKafkaSerializer::class, StringSerializer::class)
    )
    val kafkaProducerNlResponse = KafkaProducer<String, NlResponseKafkaMessage>(
        KafkaUtils
            .getAivenKafkaConfig()
            .toProducerConfig("${env.applicationName}-producer", JacksonKafkaSerializer::class, StringSerializer::class)
    )
    val nlRequestProducer = NLRequestProducer(kafkaProducer, env.beOmNLKafkaTopic)
    val nlResponseProducer = NLResponseProducer(kafkaProducerNlResponse, env.brytNLKafkaTopic)
    val beOmNyNLService = BeOmNyNLService(nlRequestProducer, nlResponseProducer, database)

    val iCorrespondenceAgencyExternalBasic = createPort(env.altinnUrl)
    val altinnClient = AltinnClient(
        username = env.altinnUsername,
        password = env.altinnPassword,
        iCorrespondenceAgencyExternalBasic = iCorrespondenceAgencyExternalBasic
    )
    val altinnOrgnummerLookup = AltinnOrgnummerLookupFacotry.getOrgnummerResolver(env.cluster)

    val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        expectSuccess = false
    }
    val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        config()
        engine {
            customizeClient {
                setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            }
        }
    }

    val basichAuthConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
        config()
        install(Auth) {
            basic {
                username = vaultSecrets.serviceuserUsername
                password = vaultSecrets.serviceuserPassword
                sendWithoutRequest = true
            }
        }
    }

    val httpClient = HttpClient(Apache, config)
    val httpClientWithAuth = HttpClient(Apache, basichAuthConfig)
    val httpClientWithProxy = HttpClient(Apache, proxyConfig)
    val pdlClient = PdlClient(
        httpClient,
        env.pdlBasePath,
        env.pdlApiKey,
        PdlClient::class.java.getResource("/graphql/getPerson.graphql").readText()
    )
    val stsOidcClient = StsOidcClient(
        username = vaultSecrets.serviceuserUsername,
        password = vaultSecrets.serviceuserPassword,
        stsUrl = env.stsOidcUrl,
        apiKey = env.stsApiKey
    )
    val accessTokenClient = AccessTokenClient(
        aadAccessTokenUrl = env.aadAccessTokenUrl,
        clientId = env.clientId,
        clientSecret = env.clientSecret,
        resource = env.narmestelederScope,
        httpClient = httpClientWithProxy
    )
    val narmestelederClient = NarmestelederClient(httpClient, accessTokenClient, env.narmesteLederBasePath)
    val narmesteLederService = NarmesteLederService(narmestelederClient, pdlClient, stsOidcClient)
    val juridiskLoggService =
        JuridiskLoggService(JuridiskLoggClient(httpClientWithAuth, env.juridiskLoggUrl, env.sykmeldingProxyApiKey))
    val altinnSendtSykmeldingService = AltinnSykmeldingService(
        altinnClient,
        altinnOrgnummerLookup,
        juridiskLoggService,
        database
    )


    val aivenKafkaSykmeldingConsumer: KafkaConsumer<String, SendSykmeldingAivenKafkaMessage> = getKafkaConsumer(env = env, resetConfig = "none")
    val aivenKafkaNarmestelederConsumer: KafkaConsumer<String, NarmestelederLeesah> = getKafkaConsumer(env = env, resetConfig = "earliest")

    val narmestelederConsumer = NarmestelederConsumer(NarmestelederDB(database), aivenKafkaNarmestelederConsumer, env.narmestelederLeesahTopic, applicationState)

    applicationState.ready = true

    narmestelederConsumer.startConsumer()

    val sendtSykmeldingAivenConsumer = SendtSykmeldingAivenConsumer(aivenKafkaSykmeldingConsumer, env.sendtSykmeldingAivenKafkaTopic)
    val sendtSykmeldingService = SendtSykmeldingService(
        applicationState,
        altinnSendtSykmeldingService,
        pdlClient,
        stsOidcClient,
        narmesteLederService,
        beOmNyNLService,
        sendtSykmeldingAivenConsumer
    )



    GlobalScope.launch(Dispatchers.IO) {
        try {
            sendtSykmeldingService.start()
        } catch (e: Exception) {
            log.error("Noe gikk galt: ", e)
        } finally {
            applicationState.ready = false
            applicationState.alive = false
        }
    }
}

private inline fun <reified T : Any> getKafkaConsumer(env: Environment, resetConfig: String = "none") = KafkaConsumer(
    KafkaUtils.getAivenKafkaConfig().also {
        it[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "100"
        it[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = resetConfig
    }.toConsumerConfig(env.applicationName + "-consumer", JacksonKafkaDeserializer::class),
    StringDeserializer(),
    JacksonKafkaDeserializer(T::class)
)
