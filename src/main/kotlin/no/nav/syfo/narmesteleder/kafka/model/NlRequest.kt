package no.nav.syfo.narmesteleder.kafka.model

import java.time.OffsetDateTime
import java.util.UUID

data class NlRequestKafkaMessage(
    val nlRequest: NlRequest,
    val nlKafkaMetadata: NlKafkaMetadata
)

data class NlRequest(
    val requestId: UUID,
    val sykmeldingId: String?,
    val fnr: String,
    val orgnr: String,
    val name: String
)

data class NlKafkaMetadata(
    val timestamp: OffsetDateTime,
    val source: String
)
