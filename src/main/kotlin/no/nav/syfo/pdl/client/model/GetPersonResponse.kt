package no.nav.syfo.pdl.client.model

import java.lang.RuntimeException

data class GetPersonResponse(
    val data: ResponseData,
)

private const val AKTORID_GRUPPE = "AKTORID"
private const val FNR_GRUPPE = "FOLKEREGISTERIDENT"

fun GetPersonResponse.toPerson(): Person {
    val navn = data.person?.navn?.first()
    val aktorId = data.identer?.identer?.first { it.gruppe == AKTORID_GRUPPE }?.ident
    val fnr = data.identer?.identer?.first { it.gruppe == FNR_GRUPPE }?.ident

    if (navn == null) {
        throw RuntimeException("Fant ikke person i PDL")
    }
    if (aktorId == null) {
        throw RuntimeException("Fant ikke aktorId i PDL")
    }
    if (fnr == null) {
        throw RuntimeException("Fant ikke fnr i PDL")
    }

    return Person(
        fornavn = navn.fornavn,
        mellomnavn = navn.mellomnavn,
        etternavn = navn.etternavn,
        aktorId = aktorId,
        fnr = fnr
    )
}

data class ResponseData(
    val person: PersonResponse?,
    val identer: IdentResponse?,
)

data class IdentResponse(
    val identer: List<Ident>,
)

data class Ident(
    val ident: String,
    val gruppe: String,
)

data class PersonResponse(
    val navn: List<Navn>?,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
)
