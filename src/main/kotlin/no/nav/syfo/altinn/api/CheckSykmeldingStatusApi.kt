package no.nav.syfo.altinn.api

import io.ktor.server.application.call
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import no.altinn.schemas.services.serviceengine.correspondence._2016._02.CorrespondenceStatusResultV3
import no.nav.syfo.altinn.AltinnClient
import no.nav.syfo.altinn.model.AltinnStatus
import no.nav.syfo.altinn.model.StatusChanges
import no.nav.syfo.securelog

fun serializeToXml(obj: CorrespondenceStatusResultV3): String {
    val objectFactory =
        no.altinn.schemas.services.serviceengine.correspondence._2016._02.ObjectFactory()
    val jaxbElement = objectFactory.createCorrespondenceStatusResultV3(obj)

    val jaxbContext =
        JAXBContext.newInstance(
            no.altinn.schemas.services.serviceengine.correspondence._2016._02.ObjectFactory::class
                .java
        )
    val marshaller: Marshaller = jaxbContext.createMarshaller()
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    val stringWriter = StringWriter()
    marshaller.marshal(jaxbElement, stringWriter)
    return stringWriter.toString()
}

fun mapXmlToObject(xml: String): AltinnStatus {
    val jaxbContext = JAXBContext.newInstance(CorrespondenceStatusResultV3::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    val xmlReader = StringReader(xml)
    val result = unmarshaller.unmarshal(xmlReader) as CorrespondenceStatusResultV3

    val statusV2 =
        result.correspondenceStatusInformation.correspondenceStatusDetailsList.statusV2
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
        statusChanges = statusChanges
    )
}

fun Routing.registerAltinnApi(altinnClient: AltinnClient) {

    get("/internal/altinn/{sykmeldingId}/{orgnummer}") {
        val altinnResult =
            altinnClient.getAltinnStatus(
                call.parameters["sykmeldingId"]!!,
                call.parameters["orgnummer"]!!
            )
        if (altinnResult == null) {
            call.respondText("No result found")
            return@get
        } else {
            val response = serializeToXml(altinnResult)
            val altinnStatus = mapXmlToObject(response)

            securelog.info("Response from altinn: $response")
            securelog.info("Mapped response: $altinnStatus")
            call.respond(altinnStatus)
        }
    }
}
