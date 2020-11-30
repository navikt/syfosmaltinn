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
import io.prometheus.client.hotspot.DefaultExports
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
import no.nav.syfo.kafka.loadBaseConfig
import no.nav.syfo.kafka.toConsumerConfig
import no.nav.syfo.narmesteleder.client.NarmestelederClient
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.SendtSykmeldingService
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage
import no.nav.syfo.sykmelding.kafka.utils.JacksonKafkaDeserializer
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.ProxySelector

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
    properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "none"
    val kafkaConsumer = KafkaConsumer<String, SendtSykmeldingKafkaMessage>(properties, StringDeserializer(), JacksonKafkaDeserializer(SendtSykmeldingKafkaMessage::class))
    val sendtSykmeldingConsumer = SendtSykmeldingConsumer(kafkaConsumer, env.sendtSykmeldingKafkaTopic)
    val iCorrespondenceAgencyExternalBasic = createPort(env.altinnUrl)
    val altinnClient = AltinnClient(username = env.altinnUsername, password = env.altinnPassword, iCorrespondenceAgencyExternalBasic = iCorrespondenceAgencyExternalBasic)
    val altinnOrgnummerLookup = AltinnOrgnummerLookupFacotry.getOrgnummerResolver(env.cluster)
    log.info("creating httpConfigs")
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
    log.info("creating pdlClient")
    val pdlClient = PdlClient(httpClient, env.pdlBasePath, env.pdlApiKey, PdlClient::class.java.getResource("/graphql/getPerson.graphql").readText())
    log.info("creating stsOidcClient")
    val stsOidcClient = StsOidcClient(username = vaultSecrets.serviceuserUsername, password = vaultSecrets.serviceuserPassword, stsUrl = env.stsOidcUrl, apiKey = env.stsApiKey)
    log.info("creating accessTokenClient")
    val accessTokenClient = AccessTokenClient(aadAccessTokenUrl = env.aadAccessTokenUrl, clientId = env.clientId, clientSecret = env.clientSecret, resource = env.narmestelederClientId, httpClient = httpClientWithProxy)
    log.info("creating narmestelederClient")
    val narmestelederClient = NarmestelederClient(httpClient, accessTokenClient, env.narmesteLederBasePath, env.sykmeldingProxyApiKey)
    val narmesteLederService = NarmesteLederService(narmestelederClient, pdlClient, stsOidcClient)
    val juridiskLoggService = JuridiskLoggService(JuridiskLoggClient(httpClientWithAuth, env.juridiskLoggUrl, env.sykmeldingProxyApiKey))
    val altinnSendtSykmeldingService = AltinnSykmeldingService(altinnClient, env, altinnOrgnummerLookup, juridiskLoggService)
    val sendtSykmeldingService = SendtSykmeldingService(applicationState, sendtSykmeldingConsumer, altinnSendtSykmeldingService, pdlClient, stsOidcClient, narmesteLederService)

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
