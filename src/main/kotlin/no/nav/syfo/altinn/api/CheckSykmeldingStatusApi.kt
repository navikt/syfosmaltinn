package no.nav.syfo.altinn.api

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import no.altinn.schemas.services.serviceengine.correspondence._2016._02.CorrespondenceStatusResultV3
import no.nav.syfo.altinn.AltinnClient
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
            securelog.info("Response from altinn: $response")
            call.respondText("check secureLog for response")
        }
    }
}
