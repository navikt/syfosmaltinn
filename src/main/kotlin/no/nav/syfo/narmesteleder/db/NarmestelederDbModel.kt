package no.nav.syfo.narmesteleder.db

import java.time.LocalDate

data class NarmestelederDbModel(
    val sykmeldtFnr: String,
    val lederFnr: String,
    val orgnummer: String,
    val narmesteLederTelefonnummer: String,
    val narmesteLederEpost: String,
    val aktivFom: LocalDate
)
