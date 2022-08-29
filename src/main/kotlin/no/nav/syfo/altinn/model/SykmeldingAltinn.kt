package no.nav.syfo.altinn.model

import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper.Companion.toSykmeldingXml
import no.nav.syfo.altinn.util.JAXB
import no.nav.syfo.altinn.util.PdfFactory
import no.nav.syfo.altinn.util.SykmeldingHTMLandPDFMapper
import no.nav.syfo.altinn.util.SykmeldingHTMLandPDFMapper.Companion.toSykmeldingHtml
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.fulltNavn
import java.io.StringWriter
import javax.xml.bind.JAXBContext

class SykmeldingAltinn(
    val xmlSykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver,
    pasient: Person,
    naemresteLeder: NarmesteLeder?
) {
    val sykmeldingXml: String = JAXB.marshallSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver)
    val sykmeldingHTML: String
    val sykmeldingPortableHTML: String
    val sykmeldingPdf: ByteArray

    init {

        val sykmeldingXmlForHtml = toSykmeldingXml(
            narmesteLeder = naemresteLeder,
            xmlSykmeldingArbeidsgiver = xmlSykmeldingArbeidsgiver
        )

        sykmeldingHTML = toSykmeldingHtml(sykmeldingXml = sykmeldingXmlForHtml)
        sykmeldingPortableHTML = SykmeldingHTMLandPDFMapper.toPortableHTML(
            sykmeldingHTML,
            xmlSykmeldingArbeidsgiver.sykmeldingId
        )

        sykmeldingPdf = PdfFactory.getSykmeldingPDF(
            sykmeldingHTML,
            pasient.fulltNavn()
        )
    }
}

fun SykmeldingAltinn.serialiser(): String {
    val writer = StringWriter()
    val context: JAXBContext = JAXBContext.newInstance(SykmeldingAltinn::class.java)
    val m = context.createMarshaller()
    m.marshal(this, writer)
    return writer.toString()
}
