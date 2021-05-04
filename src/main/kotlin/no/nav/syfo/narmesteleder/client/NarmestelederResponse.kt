package no.nav.syfo.narmesteleder.client

import java.time.LocalDate

data class NarmestelederResponse(
    val narmesteLederRelasjon: NarmesteLederRelasjon?
)

data class NarmesteLederRelasjon(
    val fnr: String,
    val orgnummer: String,
    val narmesteLederFnr: String,
    val narmesteLederTelefonnummer: String,
    val narmesteLederEpost: String,
    val aktivFom: LocalDate,
    val arbeidsgiverForskutterer: Boolean?
)
