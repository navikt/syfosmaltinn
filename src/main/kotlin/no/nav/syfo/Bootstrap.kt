package no.nav.syfo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.prometheus.client.hotspot.DefaultExports
import java.net.ProxySelector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.kafka.loadBaseConfig
import no.nav.syfo.kafka.toConsumerConfig
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.SendtSykmeldingService
import no.nav.syfo.sykmelding.altinn.AltinnSykmeldingService
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage
import no.nav.syfo.sykmelding.kafka.utils.JacksonKafkaDeserializer
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("no.nav.syfo.syfosmaltinn")

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
    applicationState.ready = true

    val vaultSecrets = VaultSecrets()
    val properties = loadBaseConfig(env, vaultSecrets).toConsumerConfig(env.applicationName + "-consumer", JacksonKafkaDeserializer::class)
    properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
    val kafkaConsumer = KafkaConsumer<String, SendtSykmeldingKafkaMessage>(properties, StringDeserializer(), JacksonKafkaDeserializer(SendtSykmeldingKafkaMessage::class))
    val sendtSykmeldingConsumer = SendtSykmeldingConsumer(kafkaConsumer, env.sendtSykmeldingKafkaTopic)
    val altinnSendtSykmeldingService = AltinnSykmeldingService()
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
    val httpClient = HttpClient(Apache, config)
    val httpClientWithProxy = HttpClient(Apache, proxyConfig)
    val pdlClient = PdlClient(httpClient, env.pdlBasePath, PdlClient::class.java.getResource("/graphql/getPerson.graphql").readText())
    val stsOidcClient = StsOidcClient(username = vaultSecrets.serviceuserUsername, password = vaultSecrets.serviceuserPassword, stsUrl = env.stsOidcUrl)
    val accessTokenClient = AccessTokenClient(aadAccessTokenUrl = env.aadAccessTokenUrl, clientId = env.clientId, clientSecret = env.clientSecret, resource = env.narmestelederClientId, httpClient = httpClientWithProxy)
    val narmestelederClient = NarmestelederClient(httpClient, accessTokenClient, env.narmesteLederBasePath)
    val sendtSykmeldingService = SendtSykmeldingService(applicationState, sendtSykmeldingConsumer, altinnSendtSykmeldingService, pdlClient, stsOidcClient, narmestelederClient)

    GlobalScope.launch {
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
