package no.nav.syfo.altinn.util

import no.nav.helse.xml.sykmeldingarbeidsgiver.ObjectFactory
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.log
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.stream.StreamResult

class JAXB private constructor() {
    companion object {
        val SYKMELDING_ARBEIDSGIVER_CONTEXT: JAXBContext = JAXBContext.newInstance(ObjectFactory::class.java)

        fun marshallSykmeldingArbeidsgiver(sykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver): String {
            try {
                val element = ObjectFactory().createSykmeldingArbeidsgiver(sykmeldingArbeidsgiver)
                val writer = StringWriter()
                val marshaller = SYKMELDING_ARBEIDSGIVER_CONTEXT.createMarshaller()
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false)
                marshaller.marshal(element, StreamResult(writer))
                return writer.toString()
            } catch (ex: JAXBException) {
                log.error("Error marshalling sykmelding")
                throw ex
            }
        }

        fun unmarshalSykmeldingArbeidsgiver(melding: String?): JAXBElement<XMLSykmeldingArbeidsgiver> {
            return try {
                val unmarshaller = SYKMELDING_ARBEIDSGIVER_CONTEXT.createUnmarshaller()
                unmarshaller.setEventHandler {
                    it.message == null
                }
                unmarshaller.unmarshal(StringReader(melding)) as JAXBElement<XMLSykmeldingArbeidsgiver>
            } catch (e: JAXBException) {
                throw RuntimeException(e)
            }
        }

        fun parseXml(xml: String): Document {
            try {
                val builderFactory = DocumentBuilderFactory.newInstance()
                builderFactory.isNamespaceAware = true
                val documentBuilder = builderFactory.newDocumentBuilder()
                return documentBuilder.parse(InputSource(StringReader(xml)))
            } catch (ex: Exception) {
                log.error("Error i parsing av XML", ex)
                throw ex
            }
        }
    }
}
