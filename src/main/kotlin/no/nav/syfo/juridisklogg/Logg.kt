package no.nav.syfo.juridisklogg

data class Logg(
    val meldingsId: String,
    val meldingsInnhold: String,
    val avsender: String,
    val mottaker: String,
    val joarkRef: String = "",
    var antallAarLagres: Number
)
