package no.nav.syfo

import no.nav.syfo.kafka.KafkaConfig
import no.nav.syfo.kafka.KafkaCredentials
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

data class Environment(
    val pdlBasePath: String = getEnvVar("PDL_BASE_PATH"),
    val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
    val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfosmaltinn"),
    val sendtSykmeldingKafkaTopic: String = "syfo-sendt-sykmelding",
    override val kafkaBootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS_URL"),
    val aadAccessTokenUrl: String = getEnvVar("AAD_ACCESS_TOKEN_URL"),
    val clientId: String = getFileAsString("/var/run/secrets/AZURE_CLIENT"),
    val clientSecret: String = getFileAsString("/var/run/secrets/AZURE_CLIENT_SECRET"),
    val narmestelederClientId: String = getEnvVar("NARMESTELEDER_CLIENT_ID"),
    val stsOidcUrl: String = getEnvVar("SECURITYTOKENSERVICE_URL"),
    val narmesteLederBasePath: String = getEnvVar("NARMESTELEDER_URL"),
    val altinnUsername: String = getFileAsString("/var/run/secrets/ALTINN_USERNAME"),
    val altinnPassword: String = getFileAsString("/var/run/secrets/ALTINN_PASSWORD"),
    val altinnUrl: String = getEnvVar("ALTINN_PORTAL_BASEURL"),
    val juridiskLoggUrl: String = getEnvVar("JURIDISKLOGG_REST_URL"),
    override val cluster: String = getEnvVar("NAIS_CLUSTER_NAME"),
    override val truststore: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
    override val truststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD"),
    val pdlApiKey: String = getEnvVar("PDL_API_KEY"),
    val stsApiKey: String? = getEnvVar("STS_API_KEY")
) : KafkaConfig

data class VaultSecrets(
    val serviceuserUsername: String = getFileAsString("/var/run/secrets/SYFOSMALTINN_USERNAME"),
    val serviceuserPassword: String = getFileAsString("/var/run/secrets/SYFOSMALTINN_PASSWORD")
) : KafkaCredentials {
    override val kafkaUsername: String = serviceuserUsername
    override val kafkaPassword: String = serviceuserPassword
}

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")

fun getFileAsString(filePath: String) = String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
