package no.nav.syfo.sykmelding.altinn.model

import no.nav.helse.xml.sykmelding.arbeidsgiver.ObjectFactory
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLAktivitet
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLArbeidsgiver
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLArbeidsutsikter
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLBehandler
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLKontaktMedPasient
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLNavn
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLPasient
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLPeriode
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLPrognose
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmelding
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLTiltak
import java.time.LocalDate
import java.time.LocalDateTime
import javax.xml.bind.JAXBElement

class SykmeldingArbeidsgiverTestUtils {

    companion object {
        private val OBJECT_FACTORY: ObjectFactory = ObjectFactory()

        fun sykmeldingArbeidsgiver(): JAXBElement<XMLSykmeldingArbeidsgiver> {
            return OBJECT_FACTORY.createSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver())
        }

        private fun xmlSykmeldingArbeidsgiver(): XMLSykmeldingArbeidsgiver? {
            val xmlSykmelding = OBJECT_FACTORY.createXMLSykmeldingArbeidsgiver()
            xmlSykmelding.juridiskOrganisasjonsnummer = "1234"
            xmlSykmelding.sykmelding = xmlSykmelding()
            xmlSykmelding.sykmeldingId = "uuid"
            xmlSykmelding.virksomhetsnummer = "orgnummer"
            return xmlSykmelding
        }

        private fun xmlSykmelding(): XMLSykmelding? {
            val xmlSykmelding = OBJECT_FACTORY.createXMLSykmelding()
            xmlSykmelding.syketilfelleFom = LocalDate.of(2016, 12, 7)
            xmlSykmelding.pasient = xmlPasient()
            xmlSykmelding.arbeidsgiver = xmlArbeidsgiver()
            xmlSykmelding.perioder.add(xmlPerioder())
            xmlSykmelding.prognose = xmlPrognose()
            xmlSykmelding.tiltak = xmlTiltak()
            xmlSykmelding.meldingTilArbeidsgiver = "MeldingTilArbeidsgiver"
            xmlSykmelding.kontaktMedPasient = xmlKontaktMedPasient()
            xmlSykmelding.behandler = xmlBehandler()
            return xmlSykmelding
        }

        private fun xmlTiltak(): XMLTiltak? {
            val tiltak = OBJECT_FACTORY.createXMLTiltak()
            tiltak.tiltakArbeidsplassen = "TiltakArbeidsplassen"
            return tiltak
        }

        private fun xmlArbeidsgiver(): XMLArbeidsgiver? {
            val xmlArbeidsgiver = OBJECT_FACTORY.createXMLArbeidsgiver()
            xmlArbeidsgiver.navn = "ArbeidsgiverNavn"
            return xmlArbeidsgiver
        }

        private fun xmlPerioder(): XMLPeriode? {
            val periode = OBJECT_FACTORY.createXMLPeriode()
            periode.fom = LocalDate.of(2016, 12, 7)
            periode.tom = LocalDate.of(2016, 12, 7)
            periode.aktivitet = xmlAktivitet()
            return periode
        }

        private fun xmlAktivitet(): XMLAktivitet? {
            val aktivitet = OBJECT_FACTORY.createXMLAktivitet()
            aktivitet.avventendeSykmelding = "AvventendeSykmelding"
            return aktivitet
        }

        private fun xmlPrognose(): XMLPrognose? {
            val prognose = OBJECT_FACTORY.createXMLPrognose()
            prognose.isErArbeidsfoerEtterEndtPeriode = false
            prognose.beskrivHensynArbeidsplassen = "BeskrivHensynArbeidsplassen"
            prognose.arbeidsutsikter = xmlArbeidsutsikter()
            return prognose
        }

        private fun xmlArbeidsutsikter(): XMLArbeidsutsikter? {
            val arbeidsutsikter = OBJECT_FACTORY.createXMLArbeidsutsikter()
            arbeidsutsikter.isHarEgetArbeidPaaSikt = true
            return arbeidsutsikter
        }

        private fun xmlBehandler(): XMLBehandler? {
            val behandler = OBJECT_FACTORY.createXMLBehandler()
            behandler.navn = xmlNavn("Behandler")
            behandler.telefonnummer = "Kontaktinformasjon"
            return behandler
        }

        private fun xmlKontaktMedPasient(): XMLKontaktMedPasient? {
            val kontaktMedPasient = OBJECT_FACTORY.createXMLKontaktMedPasient()
            kontaktMedPasient.behandlet = LocalDateTime.of(2016, 12, 7, 11, 18, 34)
            return kontaktMedPasient
        }

        private fun xmlPasient(): XMLPasient? {
            val pasient = OBJECT_FACTORY.createXMLPasient()
            pasient.navn = xmlNavn("Pasient")
            pasient.ident = "fnr"
            return pasient
        }

        private fun xmlNavn(prefix: String): XMLNavn? {
            val navn = OBJECT_FACTORY.createXMLNavn()
            navn.etternavn = prefix + "Etternavn"
            navn.mellomnavn = prefix + "Mellomnavn"
            navn.fornavn = prefix + "Fornavn"
            return navn
        }

        fun xmlSykmeldingArbeidsgiverAsString(): String? {
            return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:sykmeldingArbeidsgiver xmlns:ns2="http://nav.no/melding/virksomhet/sykmeldingArbeidsgiver/v1/sykmeldingArbeidsgiver">
    <sykmelding>
        <syketilfelleFom>2016-12-07</syketilfelleFom>
        <pasient>
            <navn>
                <etternavn>PasientEtternavn</etternavn>
                <mellomnavn>PasientMellomnavn</mellomnavn>
                <fornavn>PasientFornavn</fornavn>
            </navn>
            <ident>fnr</ident>
        </pasient>
        <arbeidsgiver>
            <navn>ArbeidsgiverNavn</navn>
        </arbeidsgiver>
        <perioder>
            <fom>2016-12-07</fom>
            <tom>2016-12-07</tom>
            <aktivitet>
                <avventendeSykmelding>AvventendeSykmelding</avventendeSykmelding>
            </aktivitet>
        </perioder>
        <prognose>
            <erArbeidsfoerEtterEndtPeriode>false</erArbeidsfoerEtterEndtPeriode>
            <beskrivHensynArbeidsplassen>BeskrivHensynArbeidsplassen</beskrivHensynArbeidsplassen>
            <arbeidsutsikter>
                <harEgetArbeidPaaSikt>true</harEgetArbeidPaaSikt>
            </arbeidsutsikter>
        </prognose>
        <tiltak>
            <tiltakArbeidsplassen>TiltakArbeidsplassen</tiltakArbeidsplassen>
        </tiltak>
        <meldingTilArbeidsgiver>MeldingTilArbeidsgiver</meldingTilArbeidsgiver>
        <kontaktMedPasient>
            <behandlet>2016-12-07T11:18:34</behandlet>
        </kontaktMedPasient>
        <behandler>
            <navn>
                <etternavn>BehandlerEtternavn</etternavn>
                <mellomnavn>BehandlerMellomnavn</mellomnavn>
                <fornavn>BehandlerFornavn</fornavn>
            </navn>
            <telefonnummer>Kontaktinformasjon</telefonnummer>
        </behandler>
    </sykmelding>
    <juridiskOrganisasjonsnummer>1234</juridiskOrganisasjonsnummer>
    <virksomhetsnummer>orgnummer</virksomhetsnummer>
    <sykmeldingId>uuid</sykmeldingId>
</ns2:sykmeldingArbeidsgiver>
"""
        }
    }
}
