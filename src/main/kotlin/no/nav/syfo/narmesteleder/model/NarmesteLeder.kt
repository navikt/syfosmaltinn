package no.nav.syfo.narmesteleder.model

import java.time.LocalDate

data class NarmesteLeder(
    val epost: String,
    val orgnummer: String,
    val telefonnummer: String,
    val aktivFom: LocalDate,
    val arbeidsgiverForskutterer: Boolean?,
    val navn: String,
    val fnr: String,
)
