package no.nav.syfo.pdl.client.model

import no.nav.syfo.log
import java.lang.Exception
import java.lang.RuntimeException

data class GetPersonResponse(
        val data: ResponseData
)

fun GetPersonResponse.toPerson(): Person {
    val navn = data.personResponse?.navn?.first()
    val aktorId = data.aktorIdResponse?.aktorIder?.first()

    if (navn == null) {
        throw RuntimeException("Fant ikke person i PDL")
    }
    if (aktorId == null) {
        throw RuntimeException("Fa ikke aktorId i PDL")
    }

    return Person(fornavn = navn.fornavn, mellomnavn = navn.mellomnavn, etternavn = navn.etternavn, aktorId = aktorId.aktorId)
}

data class ResponseData(
        val personResponse: PersonResponse?,
        val aktorIdResponse: AktorIdResponse?
)

data class AktorIdResponse(
        val aktorIder: List<AktorId>
)

data class AktorId(
        val aktorId: String
)

data class PersonResponse(
        val navn: List<Navn>?
)

data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
)

