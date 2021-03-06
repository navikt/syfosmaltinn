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
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class SykmeldingAltinn(
    sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
    pasient: Person,
    naemresteLeder: NarmesteLeder?
) {
    val xmlSykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver
    val sykmeldingXml: String
    val sykmeldingHTML: String
    val sykmeldingPortableHTML: String
    val sykmeldingPdf: ByteArray

    init {
        xmlSykmeldingArbeidsgiver = SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(
            sendtSykmeldingKafkaMessage,
            pasient
        )

        sykmeldingXml = JAXB.marshallSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver)

        val stilingsprosent = sendtSykmeldingKafkaMessage.sykmelding.arbeidsgiver.stillingsprosent

        val sykmeldingXmlForHtml = toSykmeldingXml(
            narmesteLeder = naemresteLeder,
            stillingsprosent = stilingsprosent,
            xmlSykmeldingArbeidsgiver = xmlSykmeldingArbeidsgiver
        )

        sykmeldingHTML = toSykmeldingHtml(sykmeldingXml = sykmeldingXmlForHtml)
        sykmeldingPortableHTML = SykmeldingHTMLandPDFMapper.toPortableHTML(
            sykmeldingHTML,
            sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId
        )

        sykmeldingPdf = PdfFactory.getSykmeldingPDF(
            sykmeldingHTML,
            pasient.fulltNavn()
        )
    }
}
