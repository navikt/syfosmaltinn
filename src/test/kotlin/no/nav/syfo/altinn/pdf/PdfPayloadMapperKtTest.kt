package no.nav.syfo.altinn.pdf

import io.kotest.core.spec.style.FunSpec
import no.nav.syfo.model.sykmelding.arbeidsgiver.AktivitetIkkeMuligAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.model.GradertDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.altinn.model.getSykmeldingKafkaMessage
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate
import java.util.UUID

class PdfPayloadMapperKtTest : FunSpec({
    val person = Person("Per", null, "Person", "aktorid", "fnr")
    val narmesteLeder = NarmesteLeder(
        "epost",
        "orgnummer",
        "90909090",
        LocalDate.now(),
        false,
        "Leder Ledersen",
        "fnrLeder"
    )

    context("PdfPayloadMapper") {
        test("Mapper sykmelding med tre perioder riktig") {
            val sykmeldingId = UUID.randomUUID().toString()
            val perioder = listOf(
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 7),
                    tom = LocalDate.of(2022, 1, 15),
                    gradert = GradertDTO(50, false),
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.GRADERT,
                    aktivitetIkkeMulig = null,
                    reisetilskudd = false
                ),
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 1),
                    tom = LocalDate.of(2022, 1, 6),
                    gradert = null,
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                    aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                    reisetilskudd = false
                ),
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 16),
                    tom = LocalDate.of(2022, 1, 20),
                    gradert = null,
                    behandlingsdager = 1,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.BEHANDLINGSDAGER,
                    aktivitetIkkeMulig = null,
                    reisetilskudd = false
                )
            )
            val sykmeldingKafkaMessage = getSykmeldingKafkaMessage(sykmeldingId, perioder)

            val pdfPayload = sykmeldingKafkaMessage.sykmelding.toPdfPayload(person, narmesteLeder)

            pdfPayload.ansatt shouldBeEqualTo Ansatt("fnr", "Per Person")
            pdfPayload.narmesteleder shouldBeEqualTo narmesteLeder
            pdfPayload.arbeidsgiverSykmelding.arbeidsgiverNavn shouldBeEqualTo "ArbeidsgiverNavn"
            pdfPayload.arbeidsgiverSykmelding.prognose shouldBeEqualTo sykmeldingKafkaMessage.sykmelding.prognose
            pdfPayload.arbeidsgiverSykmelding.tiltakArbeidsplassen shouldBeEqualTo sykmeldingKafkaMessage.sykmelding.tiltakArbeidsplassen
            pdfPayload.arbeidsgiverSykmelding.meldingTilArbeidsgiver shouldBeEqualTo sykmeldingKafkaMessage.sykmelding.meldingTilArbeidsgiver
            pdfPayload.arbeidsgiverSykmelding.behandler shouldBeEqualTo BehandlerPdf(
                "Behandlerfornavn Behandlermellomnavn Behandleretternavn",
                "telefon"
            )
            pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.first() shouldBeEqualTo SykmeldingsperiodePdf(
                fom = LocalDate.of(2022, 1, 1),
                tom = LocalDate.of(2022, 1, 6),
                varighet = 6,
                gradert = null,
                behandlingsdager = null,
                innspillTilArbeidsgiver = null,
                type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                reisetilskudd = false
            )
            pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder[1] shouldBeEqualTo SykmeldingsperiodePdf(
                fom = LocalDate.of(2022, 1, 7),
                tom = LocalDate.of(2022, 1, 15),
                varighet = 9,
                gradert = GradertDTO(50, false),
                behandlingsdager = null,
                innspillTilArbeidsgiver = null,
                type = PeriodetypeDTO.GRADERT,
                aktivitetIkkeMulig = null,
                reisetilskudd = false
            )
            pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.last() shouldBeEqualTo SykmeldingsperiodePdf(
                fom = LocalDate.of(2022, 1, 16),
                tom = LocalDate.of(2022, 1, 20),
                varighet = 5,
                gradert = null,
                behandlingsdager = 1,
                innspillTilArbeidsgiver = null,
                type = PeriodetypeDTO.BEHANDLINGSDAGER,
                aktivitetIkkeMulig = null,
                reisetilskudd = false
            )
        }
    }
})
