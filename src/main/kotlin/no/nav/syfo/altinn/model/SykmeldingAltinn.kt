package no.nav.syfo.altinn.model

import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper.Companion.toSykmeldingXml
import no.nav.syfo.altinn.util.JAXB
import no.nav.syfo.altinn.util.SykmeldingHTMLMapper
import no.nav.syfo.altinn.util.SykmeldingHTMLMapper.Companion.toSykmeldingHtml
import no.nav.syfo.narmesteleder.model.NarmesteLeder

class SykmeldingAltinn(
    val xmlSykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver,
    narmesteLeder: NarmesteLeder?,
    pdf: ByteArray,
) {
    val sykmeldingXml: String = JAXB.marshallSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver)
    val sykmeldingHTML: String
    val sykmeldingPortableHTML: String
    val sykmeldingPdf: ByteArray = pdf

    init {

        val sykmeldingXmlForHtml = toSykmeldingXml(
            narmesteLeder = narmesteLeder,
            xmlSykmeldingArbeidsgiver = xmlSykmeldingArbeidsgiver,
        )

        sykmeldingHTML = toSykmeldingHtml(sykmeldingXml = sykmeldingXmlForHtml)
        sykmeldingPortableHTML = SykmeldingHTMLMapper.toPortableHTML(
            sykmeldingHTML,
            xmlSykmeldingArbeidsgiver.sykmeldingId,
        )
    }
}
