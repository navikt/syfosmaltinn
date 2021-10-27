package no.nav.syfo.sykmelding.altinn.model

import getSykmeldingKafkaMessage
import java.time.LocalDate
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnSykmeldingMapperTest : Spek({
    describe("Test sykmelding mapper") {
        it("Shuold create InsertCorrespondenceV2") {
            val sykmeldingId = "uuid"
            val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage(sykmeldingId)
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr"
            )
            val narmesteLEder = NarmesteLeder(
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                "NL Navn", "fnrLeder"
            )
            val sykmeldingAltinn = SykmeldingAltinn(
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, pasient), pasient, narmesteLEder
            )

            val insertCorrespondanceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
                sykmeldingAltinn,
                sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" "),
                sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer
            )
            insertCorrespondanceV2 shouldNotBe null
        }
    }
})
