package no.nav.syfo.altinn.util

import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamResult
import no.nav.helse.xml.sykmelding.arbeidsgiver.ObjectFactory
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.exception.AltinnException
import org.w3c.dom.Document
import org.xml.sax.InputSource

class JAXB private constructor() {
    companion object {
        val SYKMELDING_ARBEIDSGIVER_CONTEXT: JAXBContext =
            JAXBContext.newInstance(ObjectFactory::class.java)

        fun marshallSykmeldingArbeidsgiver(
            sykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver
        ): String {
            try {
                val element = ObjectFactory().createSykmeldingArbeidsgiver(sykmeldingArbeidsgiver)
                val writer = StringWriter()
                val marshaller = SYKMELDING_ARBEIDSGIVER_CONTEXT.createMarshaller()
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false)
                marshaller.marshal(element, StreamResult(writer))
                return writer.toString()
            } catch (ex: JAXBException) {
                throw AltinnException("Error marshalling sykmelding", ex)
            }
        }

        fun parseXml(xml: String): Document {
            try {
                val builderFactory = DocumentBuilderFactory.newInstance()
                builderFactory.isNamespaceAware = true
                val documentBuilder = builderFactory.newDocumentBuilder()
                return documentBuilder.parse(InputSource(StringReader(xml)))
            } catch (ex: Exception) {
                throw AltinnException("Error i parsing av XML", ex)
            }
        }
    }
}
