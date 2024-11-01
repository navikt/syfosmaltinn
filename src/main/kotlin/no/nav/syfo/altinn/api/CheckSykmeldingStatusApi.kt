package no.nav.syfo.altinn.api

import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.altinn.schemas.services.serviceengine.correspondence._2016._02.CorrespondenceStatusResultV3
import no.nav.syfo.altinn.AltinnClient
import no.nav.syfo.altinn.model.AltinnStatus
import no.nav.syfo.altinn.model.StatusChanges
import no.nav.syfo.logger
import no.nav.syfo.securelog

fun mapToAltinnStatus(correspondenceStatusResultV3: CorrespondenceStatusResultV3): AltinnStatus {

    val statusV2 =
        correspondenceStatusResultV3.correspondenceStatusInformation.correspondenceStatusDetailsList
            .statusV2
            .firstOrNull()
    val statusChanges =
        statusV2
            ?.statusChanges
            ?.statusChangeV2
            ?.map { StatusChanges(date = it.statusDate.toString(), type = it.statusType.value()) }
            ?.toSet()
            ?: emptySet()

    return AltinnStatus(
        correspondenceId = (statusV2?.correspondenceID ?: "").toString(),
        createdDate = statusV2?.createdDate?.toString() ?: "",
        orgnummer = statusV2?.reportee ?: "",
        sendersReference = statusV2?.sendersReference ?: "",
        statusChanges = statusChanges,
    )
}

fun Route.registerAltinnApi(altinnClient: AltinnClient) {
    route("/internal") {
        get("/altinn/{sykmeldingId}/{orgnummer}") {
            val sykmeldingId = call.parameters["sykmeldingId"]!!
            val orgnummer = call.parameters["orgnummer"]!!

            val altinnResult = altinnClient.getAltinnStatus(sykmeldingId, orgnummer)
            if (
                altinnResult == null ||
                    altinnResult.correspondenceStatusInformation.correspondenceStatusDetailsList
                        .statusV2
                        .isNullOrEmpty()
            ) {
                logger.info(
                    "No result found for sykmeldingid: $sykmeldingId, orgnummer: $orgnummer"
                )
                call.respond(
                    HttpStatusCode.NotFound,
                    "No result found for sykmeldingid: $sykmeldingId, orgnummer: $orgnummer"
                )
            } else {
                logger.info(
                    "Got altinnResult for sykmeldingid: $sykmeldingId, orgnummer: $orgnummer"
                )

                val altinnStatus = mapToAltinnStatus(altinnResult)

                securelog.info("Mapped response: $altinnStatus")
                call.respond(altinnStatus)
            }
        }
    }
}
