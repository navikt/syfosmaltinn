package no.nav.syfo.altinn.model

import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.kafka.EnkelSykmelding
import no.nav.syfo.model.sykmelding.model.AdresseDTO
import no.nav.syfo.model.sykmelding.model.ArbeidsgiverDTO
import no.nav.syfo.model.sykmelding.model.BehandlerDTO
import no.nav.syfo.model.sykmelding.model.ErIArbeidDTO
import no.nav.syfo.model.sykmelding.model.KontaktMedPasientDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.model.sykmelding.model.PrognoseDTO
import no.nav.syfo.model.sykmelding.model.SykmeldingsperiodeDTO
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnSykmeldingMapperTest : Spek({
    describe("Test sykmelding mapper") {
        it("Shuold create InsertCorrespondenceV2") {
            val sykmeldingId = "uuid"
            val sendtSykmeldingKafkaMessage = SendtSykmeldingKafkaMessage(
                sykmelding = EnkelSykmelding(
                    id = sykmeldingId,
                    arbeidsgiver = ArbeidsgiverDTO("ArbeidsgiverNavn", 100),
                    behandler = BehandlerDTO(
                        "BehandlerFornavn", "BehandlerMellomnavn", "BehandlerEtternavn", "aktorid", "12345678901", "7898789", null, AdresseDTO(
                            null,
                            null,
                            null,
                            null,
                            null
                        ), "Kontaktinformasjon"
                    ),
                    behandletTidspunkt = OffsetDateTime.now(),
                    egenmeldt = false,
                    harRedusertArbeidsgiverperiode = false,
                    kontaktMedPasient = KontaktMedPasientDTO(LocalDate.of(2016, 12, 7), null),
                    legekontorOrgnummer = null,
                    meldingTilArbeidsgiver = "MeldingTilArbeidsgiver",
                    mottattTidspunkt = OffsetDateTime.now(),
                    navnFastlege = null,
                    papirsykmelding = false,
                    prognose = PrognoseDTO(
                        arbeidsforEtterPeriode = false,
                        hensynArbeidsplassen = "BeskrivHensynArbeidsplassen",
                        erIArbeid = ErIArbeidDTO(
                            egetArbeidPaSikt = true,
                            annetArbeidPaSikt = false,
                            arbeidFOM = null,
                            vurderingsdato = null
                        ),
                        erIkkeIArbeid = null
                    ),
                    syketilfelleStartDato = LocalDate.of(2016, 12, 7),
                    sykmeldingsperioder = listOf(
                        SykmeldingsperiodeDTO(
                            LocalDate.of(2016, 12, 7),
                            LocalDate.of(2016, 12, 7),
                            null,
                            null,
                            "AvventendeSykmelding",
                            PeriodetypeDTO.AVVENTENDE,
                            null,
                            false
                        )
                    ),
                    tiltakArbeidsplassen = "TiltakArbeidsplassen"
                ),
                event = SykmeldingStatusKafkaEventDTO(
                    sykmeldingId = sykmeldingId,
                    arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
                    sporsmals = emptyList(),
                    statusEvent = "SENDT",
                    timestamp = OffsetDateTime.now()
                ),
                kafkaMetadata = KafkaMetadataDTO(sykmeldingId, OffsetDateTime.now(), "fnr", "syfoservice")
            )
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr"
            )
            val narmesteLEder = NarmesteLeder(
                "aktoerIdLeder",
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                null, null, "NL Navn", "fnrLeder"
            )
            val sykmeldingAltinn = SykmeldingAltinn(
                sendtSykmeldingKafkaMessage, pasient, narmesteLEder
            )

            val insertCorrespondanceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
                sykmeldingAltinn,
                sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" ")
            )
            insertCorrespondanceV2 shouldNotBe null
        }
    }
})
