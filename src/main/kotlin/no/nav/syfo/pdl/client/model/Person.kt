package no.nav.syfo.pdl.client.model

data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val aktorId: String,
    val fnr: String
)

fun Person.fulltNavn(): String {
    return if (mellomnavn.isNullOrEmpty()) {
        capitalizeFirstLetter("$fornavn $etternavn")
    } else {
        capitalizeFirstLetter("$fornavn $mellomnavn $etternavn")
    }
}

fun capitalizeFirstLetter(string: String): String {
    return string.toLowerCase()
        .split(" ").joinToString(" ") { it.capitalize() }
        .split("-").joinToString("-") { it.capitalize() }.trimEnd()
}
