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
    val beOmNLKafkaTopic: String = "teamsykmelding.syfo-nl-request",
    val brytNLKafkaTopic: String = "teamsykmelding.syfo-narmesteleder",
    override val kafkaBootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS_URL"),
    val aadAccessTokenUrl: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val narmestelederScope: String = getEnvVar("NARMESTELEDER_SCOPE"),
    val stsOidcUrl: String = getEnvVar("SECURITYTOKENSERVICE_URL"),
    val narmesteLederBasePath: String = getEnvVar("NARMESTELEDER_URL"),
    val altinnUsername: String = getFileAsString("/var/run/secrets/ALTINN_USERNAME"),
    val altinnPassword: String = getFileAsString("/var/run/secrets/ALTINN_PASSWORD"),
    val altinnUrl: String = getEnvVar("ALTINN_URL"),
    val juridiskLoggUrl: String = getEnvVar("JURIDISKLOGG_REST_URL"),
    override val cluster: String = getEnvVar("NAIS_CLUSTER_NAME"),
    override val truststore: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
    override val truststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD"),
    val pdlApiKey: String = getEnvVar("PDL_API_KEY"),
    val stsApiKey: String? = getEnvVar("STS_API_KEY"),
    val sykmeldingProxyApiKey: String = getEnvVar("SYKMELDING_FSS_PROXY_API_KEY"),
    val databaseUsername: String = getEnvVar("NAIS_DATABASE_USERNAME"),
    val databasePassword: String = getEnvVar("NAIS_DATABASE_PASSWORD"),
    val dbHost: String = getEnvVar("NAIS_DATABASE_HOST"),
    val dbPort: String = getEnvVar("NAIS_DATABASE_PORT"),
    val dbName: String = getEnvVar("NAIS_DATABASE_DATABASE")
) : KafkaConfig {
    fun jdbcUrl(): String {
        return "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    }
}

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
