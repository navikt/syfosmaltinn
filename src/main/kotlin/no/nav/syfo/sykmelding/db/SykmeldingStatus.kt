package no.nav.syfo.sykmelding.db

import java.time.OffsetDateTime

data class SykmeldingStatus(
    val sykmeldingId: String,
    val altinnTimestamp: OffsetDateTime?,
    val loggTimestamp: OffsetDateTime?
)
