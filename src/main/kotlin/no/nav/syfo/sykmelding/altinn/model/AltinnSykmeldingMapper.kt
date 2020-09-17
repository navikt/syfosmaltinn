package no.nav.syfo.sykmelding.altinn.model

import java.time.LocalTime
import no.nav.helse.xml.sykmeldingarbeidsgiver.ObjectFactory
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLAktivitet
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLAktivitetIkkeMulig
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLArbeidsgiver
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLArbeidsutsikter
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLBehandler
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLGradertSykmelding
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLKontaktMedPasient
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLNavn
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPasient
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPeriode
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPrognose
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmelding
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLTiltak
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.pdl.client.model.Navn
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage
import no.nav.syfo.sykmelding.model.AktivitetIkkeMuligDTO
import no.nav.syfo.sykmelding.model.ArbeidsgiverDTO
import no.nav.syfo.sykmelding.model.ArbeidsrelatertArsakTypeDTO
import no.nav.syfo.sykmelding.model.BehandlerDTO
import no.nav.syfo.sykmelding.model.ErIArbeidDTO
import no.nav.syfo.sykmelding.model.GradertDTO
import no.nav.syfo.sykmelding.model.KontaktMedPasientDTO
import no.nav.syfo.sykmelding.model.PrognoseDTO
import no.nav.syfo.sykmelding.model.SykmeldingsperiodeDTO

class AltinnSykmeldingMapper private constructor() {
    companion object {
        fun toAltinnXMLSykmelding(
                sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
                person: Person
        ): XMLSykmeldingArbeidsgiver {
            val xmlSykmeldingArbeidsgiver = ObjectFactory().createXMLSykmeldingArbeidsgiver()
            xmlSykmeldingArbeidsgiver.juridiskOrganisasjonsnummer =
                sendtSykmeldingKafkaMessage.event.arbeidsgiver!!.juridiskOrgnummer
            xmlSykmeldingArbeidsgiver.mottattidspunkt = sendtSykmeldingKafkaMessage.sykmelding.mottattTidspunkt.toLocalDateTime()
            xmlSykmeldingArbeidsgiver.sykmeldingId = sendtSykmeldingKafkaMessage.sykmelding.id
            xmlSykmeldingArbeidsgiver.virksomhetsnummer = sendtSykmeldingKafkaMessage.event.arbeidsgiver!!.orgnummer
            xmlSykmeldingArbeidsgiver.sykmelding = toXMLSykmelding(sendtSykmeldingKafkaMessage, person)
            return xmlSykmeldingArbeidsgiver
        }

        private fun toXMLSykmelding(
            sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
            person: Person
        ): XMLSykmelding {
            val sendtSykmelding = sendtSykmeldingKafkaMessage.sykmelding
            val xmlSykmelding = ObjectFactory().createXMLSykmelding()
            xmlSykmelding.arbeidsgiver = getArbeidsgiver(sendtSykmelding.arbeidsgiver)
            xmlSykmelding.behandler = getBehandler(sendtSykmelding.behandler)
            xmlSykmelding.kontaktMedPasient = getKontaktMedPasient(sendtSykmelding.kontaktMedPasient)
            xmlSykmelding.meldingTilArbeidsgiver = getMeldingTilArbeidsgiver(sendtSykmelding.meldingTilArbeidsgiver)
            xmlSykmelding.pasient = getPasient(sendtSykmeldingKafkaMessage.kafkaMetadata, person)
            xmlSykmelding.perioder.addAll(getPerioder(sendtSykmelding.sykmeldingsperioder))
            xmlSykmelding.prognose = getPrognose(sendtSykmelding.prognose)
            xmlSykmelding.syketilfelleFom = sendtSykmelding.syketilfelleStartDato
            xmlSykmelding.tiltak = getTiltak(sendtSykmelding.tiltakArbeidsplassen)
            return xmlSykmelding
        }

        private fun getTiltak(tiltakArbeidsplassen: String?): XMLTiltak? {
            return when (tiltakArbeidsplassen) {
                null -> null
                else -> {
                    val xmlTiltak = ObjectFactory().createXMLTiltak()
                    xmlTiltak.tiltakArbeidsplassen = tiltakArbeidsplassen
                    xmlTiltak
                }
            }
        }

        private fun getPrognose(prognose: PrognoseDTO?): XMLPrognose? {
            return when (prognose) {
                null -> null
                else -> {
                    val xmlPrognose = ObjectFactory().createXMLPrognose()
                    xmlPrognose.isErArbeidsfoerEtterEndtPeriode = prognose.arbeidsforEtterPeriode
                    xmlPrognose.beskrivHensynArbeidsplassen = prognose.hensynArbeidsplassen
                    xmlPrognose.arbeidsutsikter = getArbeidsUtsikter(prognose.erIArbeid)
                    xmlPrognose
                }
            }
        }

        private fun getArbeidsUtsikter(erIArbeid: ErIArbeidDTO?): XMLArbeidsutsikter? {
            return when (erIArbeid) {
                null -> null
                else -> {
                    val xmlArbeidsutsikter = ObjectFactory().createXMLArbeidsutsikter()
                    xmlArbeidsutsikter.arbeidFom = erIArbeid.arbeidFOM
                    xmlArbeidsutsikter.isHarEgetArbeidPaaSikt = erIArbeid.egetArbeidPaSikt
                    xmlArbeidsutsikter.isHarAnnetArbeidPaaSikt = erIArbeid.annetArbeidPaSikt
                    xmlArbeidsutsikter
                }
            }
        }

        private fun getPerioder(sykmeldingsperioder: List<SykmeldingsperiodeDTO>): List<XMLPeriode> {
            return sykmeldingsperioder.map {
                val periode = XMLPeriode()
                periode.fom = it.fom
                periode.tom = it.tom
                periode.aktivitet = getAktivitet(it)
                periode
            }
        }

        private fun getAktivitet(it: SykmeldingsperiodeDTO): XMLAktivitet {
            val xmlAktivitet = ObjectFactory().createXMLAktivitet()
            xmlAktivitet.avventendeSykmelding = it.innspillTilArbeidsgiver
            xmlAktivitet.gradertSykmelding = getGradertAktivitet(it.gradert)
            xmlAktivitet.aktivitetIkkeMulig = getAktivitetIkkeMulig(it.aktivitetIkkeMulig)
            xmlAktivitet.isHarReisetilskudd = it.reisetilskudd
            xmlAktivitet.antallBehandlingsdagerUke = it.behandlingsdager
            return xmlAktivitet
        }

        private fun getAktivitetIkkeMulig(aktivitetIkkeMulig: AktivitetIkkeMuligDTO?): XMLAktivitetIkkeMulig? {
            return when (aktivitetIkkeMulig) {
                null -> null
                else -> {
                    val xmlAktivitetIkkeMulig = ObjectFactory().createXMLAktivitetIkkeMulig()
                    xmlAktivitetIkkeMulig.isManglendeTilretteleggingPaaArbeidsplassen =
                        isMangledneTilrettelegging(aktivitetIkkeMulig)
                    xmlAktivitetIkkeMulig.beskrivelse = aktivitetIkkeMulig.arbeidsrelatertArsak?.beskrivelse
                    xmlAktivitetIkkeMulig
                }
            }
        }

        private fun isMangledneTilrettelegging(aktivitetIkkeMulig: AktivitetIkkeMuligDTO): Boolean? {
            return aktivitetIkkeMulig.arbeidsrelatertArsak?.arsak?.stream()
                ?.anyMatch { it == ArbeidsrelatertArsakTypeDTO.MANGLENDE_TILRETTELEGGING }
        }

        private fun getGradertAktivitet(gradert: GradertDTO?): XMLGradertSykmelding? {
            return when (gradert) {
                null -> null
                else -> {
                    val xmlGradertSykmelding = ObjectFactory().createXMLGradertSykmelding()
                    xmlGradertSykmelding.sykmeldingsgrad = gradert.grad
                    xmlGradertSykmelding.isHarReisetilskudd = gradert.reisetilskudd
                    xmlGradertSykmelding
                }
            }
        }

        private fun getPasient(
            metadata: KafkaMetadataDTO,
            person: Person
        ): XMLPasient? {
            val pasient = ObjectFactory().createXMLPasient()
            pasient.ident = metadata.fnr
            val xmlNavn = XMLNavn()
            xmlNavn.fornavn = person.fornavn
            xmlNavn.mellomnavn = person.mellomnavn
            xmlNavn.etternavn = person.etternavn
            return pasient
        }

        private fun getMeldingTilArbeidsgiver(meldingTilArbeidsgiver: String?): String? {
            return meldingTilArbeidsgiver
        }

        private fun getKontaktMedPasient(kontaktMedPasient: KontaktMedPasientDTO): XMLKontaktMedPasient? {
            val xmlKontaktMedPasient = ObjectFactory().createXMLKontaktMedPasient()
            xmlKontaktMedPasient.behandlet = kontaktMedPasient.kontaktDato?.atTime(LocalTime.NOON)
            return xmlKontaktMedPasient
        }

        private fun getBehandler(behandler: BehandlerDTO): XMLBehandler? {
            val xmlBehandler = ObjectFactory().createXMLBehandler()
            xmlBehandler.navn = getNavn(behandler)
            xmlBehandler.telefonnummer = behandler.tlf
            return xmlBehandler
        }

        private fun getNavn(behandler: BehandlerDTO): XMLNavn? {
            val xmlNavn = ObjectFactory().createXMLNavn()
            xmlNavn.fornavn = behandler.fornavn
            xmlNavn.etternavn = behandler.etternavn
            xmlNavn.mellomnavn = behandler.mellomnavn
            return xmlNavn
        }

        private fun getArbeidsgiver(arbeidsgiver: ArbeidsgiverDTO): XMLArbeidsgiver? {
            val xmlArbeidsgiver = ObjectFactory().createXMLArbeidsgiver()
            xmlArbeidsgiver.navn = arbeidsgiver.navn
            return xmlArbeidsgiver
        }
    }
}
