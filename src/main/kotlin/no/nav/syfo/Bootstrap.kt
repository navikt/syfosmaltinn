package no.nav.syfo

import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    applicationState.ready = true

//    val vaultSecrets = VaultSecrets()
//    val properties = loadBaseConfig(env, vaultSecrets).toConsumerConfig(env.applicationName + "-consumer", JacksonKafkaDeserializer::class)
//    properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
//    val kafkaConsumer = KafkaConsumer<String, SendtSykmeldingKafkaMessage>(properties, StringDeserializer(), JacksonKafkaDeserializer(SendtSykmeldingKafkaMessage::class))
//    val sendtSykmeldingConsumer = SendtSykmeldingConsumer(kafkaConsumer, env.sendtSykmeldingKafkaTopic)
//    val iCorrespondenceAgencyExternalBasic = createPort(env.altinnUrl).apply {
//        stsClient(env.altinSTSUrl, vaultSecrets.serviceuserUsername to vaultSecrets.serviceuserPassword).configureFor(this)
//    }
//    val altinnClient = AltinnClient(username = env.altinnUsername, password = env.altinnPassword, iCorrespondenceAgencyExternalBasic = iCorrespondenceAgencyExternalBasic)
//    val altinnReporteeLookup = AltinnReporteeLookupFacotry.getReporteeResolver(env.cluster)
//
//    val config: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
//        install(JsonFeature) {
//            serializer = JacksonSerializer {
//                registerKotlinModule()
//                registerModule(JavaTimeModule())
//                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
//                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            }
//        }
//        expectSuccess = false
//    }
//    val proxyConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
//        config()
//        engine {
//            customizeClient {
//                setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault()))
//            }
//        }
//    }
//
//    val basichAuthConfig: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {
//        config()
//        install(Auth) {
//            basic {
//                username = vaultSecrets.serviceuserUsername
//                password = vaultSecrets.serviceuserPassword
//                sendWithoutRequest = true
//            }
//        }
//    }
//    val httpClient = HttpClient(Apache, config)
//    val httpClientWithAuth = HttpClient(Apache, basichAuthConfig)
//    val httpClientWithProxy = HttpClient(Apache, proxyConfig)
//    val pdlClient = PdlClient(httpClient, env.pdlBasePath, PdlClient::class.java.getResource("/graphql/getPerson.graphql").readText())
//    val stsOidcClient = StsOidcClient(username = vaultSecrets.serviceuserUsername, password = vaultSecrets.serviceuserPassword, stsUrl = env.stsOidcUrl)
//    val accessTokenClient = AccessTokenClient(aadAccessTokenUrl = env.aadAccessTokenUrl, clientId = env.clientId, clientSecret = env.clientSecret, resource = env.narmestelederClientId, httpClient = httpClientWithProxy)
//    val narmestelederClient = NarmestelederClient(httpClient, accessTokenClient, env.narmesteLederBasePath)
//    val narmesteLederService = NarmesteLederService(narmestelederClient, pdlClient, stsOidcClient)
//    val juridiskLoggService = JuridiskLoggService(JuridiskLoggClient(httpClientWithAuth, env.juridiskLoggUrl))
//    val altinnSendtSykmeldingService = AltinnSykmeldingService(altinnClient, env, altinnReporteeLookup, juridiskLoggService)
//    val sendtSykmeldingService = SendtSykmeldingService(applicationState, sendtSykmeldingConsumer, altinnSendtSykmeldingService, pdlClient, stsOidcClient, narmesteLederService)
//
//    GlobalScope.launch {
//        try {
//            sendtSykmeldingService.start()
//        } catch (e: Exception) {
//            log.error("Noe gikk galt: ", e)
//        } finally {
//            applicationState.ready = false
//            applicationState.alive = false
//        }
//    }
}
