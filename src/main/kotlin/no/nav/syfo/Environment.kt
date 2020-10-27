package no.nav.syfo

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import no.nav.syfo.kafka.KafkaConfig
import no.nav.syfo.kafka.KafkaCredentials

data class Environment(
    val pdlBasePath: String = getEnvVar("PDL_BASE_PATH"),
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfosmaltinn"),
    val sendtSykmeldingKafkaTopic: String = "syfo-sendt-sykmelding",
    override val kafkaBootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS_URL"),
    val aadAccessTokenUrl: String = getEnvVar("AAD_ACCESS_TOKEN_URL"),
    val clientId: String = getFileAsString("/secrets/azuread/client_id"),
    val clientSecret: String = getFileAsString("/secrets/azuread/client_secret"),
    val narmestelederClientId: String = getEnvVar("NARMESTELEDER_CLIENT_ID"),
    val stsOidcUrl: String = "http://security-token-service.default/rest/v1/sts/token",
    val narmesteLederBasePath: String = "http://syfonarmesteleder.default",
    val altinnUsername: String = getFileAsString("/secrets/vault/altinn_username"),
    val altinnPassword: String = getFileAsString("/secrets/vault/altinn_secret"),
    val altinnUrl: String = getEnvVar("EKSTERN_ALTINN_BEHANDLEALTINNMELDING_V1_ENDPOINTURL"),
    val altinSTSUrl: String = getEnvVar("SECURITYTOKENSERVICE_URL"),
    val juridiskLoggUrl: String = getEnvVar("JURIDISKLOGG_REST_URL"),
    override val cluster: String = getEnvVar("NAIS_CLUSTER_NAME"),
    override val truststore: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
    override val truststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD")
) : KafkaConfig

data class VaultSecrets(
    val serviceuserUsername: String = getFileAsString("/secrets/serviceuser/username"),
    val serviceuserPassword: String = getFileAsString("/secrets/serviceuser/password")
) : KafkaCredentials {
    override val kafkaUsername: String = serviceuserUsername
    override val kafkaPassword: String = serviceuserPassword
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

fun getFileAsString(filePath: String) = String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
