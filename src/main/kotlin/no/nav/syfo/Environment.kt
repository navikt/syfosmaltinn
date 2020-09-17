package no.nav.syfo

import no.nav.syfo.kafka.KafkaConfig
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import no.nav.syfo.kafka.KafkaCredentials

data class Environment(
        val pdlBasePath: String = getEnvVar("PDL_BASE_PATH"),
        val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
        val applicationName: String = getEnvVar("NAIS_APP_NAME", "syfosmaltinn"),
        val sendtSykmeldingKafkaTopic: String = "privat-syfo-sendt-sykmelding",
        override val kafkaBootstrapServers: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS_URL"),
        val aadAccessTokenUrl: String = getEnvVar("AAD_ACCESS_TOKEN_URL"),
        val clientId: String = getFileAsString("/secrets/azuread/client_id"),
        val clientSecret: String = getFileAsString("/secrets/azuread/client_secret"),
        val narmestelederClientId: String = getEnvVar("NARMESTELEDER_CLIENT_ID")
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
