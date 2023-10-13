package no.nav.syfo.altinn.pdf

import java.time.LocalDate
import java.util.UUID
import no.nav.syfo.model.sykmelding.arbeidsgiver.AktivitetIkkeMuligAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.UtenlandskSykmeldingAGDTO
import no.nav.syfo.model.sykmelding.model.GradertDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.altinn.model.getSykmeldingKafkaMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class PdfPayloadMapperKtTest {
    private val person = Person("Per", null, "Person", "aktorid", "fnr")
    private val narmesteLeder =
        NarmesteLeder(
            "epost",
            "orgnummer",
            "90909090",
            LocalDate.now(),
            false,
            "Leder Ledersen",
            "fnrLeder",
        )

    @Test
    internal fun `Mapper sykmelding med tre perioder riktig`() {
        val sykmeldingId = UUID.randomUUID().toString()
        val perioder =
            listOf(
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 7),
                    tom = LocalDate.of(2022, 1, 15),
                    gradert = GradertDTO(50, false),
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.GRADERT,
                    aktivitetIkkeMulig = null,
                    reisetilskudd = false,
                ),
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 1),
                    tom = LocalDate.of(2022, 1, 6),
                    gradert = null,
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                    aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                    reisetilskudd = false,
                ),
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 1, 16),
                    tom = LocalDate.of(2022, 1, 20),
                    gradert = null,
                    behandlingsdager = 1,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.BEHANDLINGSDAGER,
                    aktivitetIkkeMulig = null,
                    reisetilskudd = false,
                ),
            )
        val sykmeldingKafkaMessage = getSykmeldingKafkaMessage(sykmeldingId, perioder)

        val pdfPayload =
            sykmeldingKafkaMessage.sykmelding.toPdfPayload(
                person,
                narmesteLeder,
                emptyList(),
            )

        pdfPayload.ansatt shouldBeEqualTo Ansatt("fnr", "Per Person")
        pdfPayload.narmesteleder shouldBeEqualTo narmesteLeder
        pdfPayload.arbeidsgiverSykmelding.arbeidsgiverNavn shouldBeEqualTo "ArbeidsgiverNavn"
        pdfPayload.arbeidsgiverSykmelding.prognose shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.prognose
        pdfPayload.arbeidsgiverSykmelding.tiltakArbeidsplassen shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.tiltakArbeidsplassen
        pdfPayload.arbeidsgiverSykmelding.meldingTilArbeidsgiver shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.meldingTilArbeidsgiver
        pdfPayload.arbeidsgiverSykmelding.behandler shouldBeEqualTo
            BehandlerPdf(
                "Behandlerfornavn Behandlermellomnavn Behandleretternavn",
                "telefon",
            )
        pdfPayload.arbeidsgiverSykmelding.egenmeldingsdager?.size shouldBeEqualTo 0
        val forstePeriode = pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.first()
        forstePeriode.fom shouldBeEqualTo LocalDate.of(2022, 1, 1)
        forstePeriode.tom shouldBeEqualTo LocalDate.of(2022, 1, 6)
        forstePeriode.varighet shouldBeEqualTo 6
        forstePeriode.gradert shouldBeEqualTo null
        forstePeriode.behandlingsdager shouldBeEqualTo null
        forstePeriode.innspillTilArbeidsgiver shouldBeEqualTo null
        forstePeriode.type shouldBeEqualTo PeriodetypeDTO.AKTIVITET_IKKE_MULIG
        forstePeriode.aktivitetIkkeMulig shouldBeEqualTo AktivitetIkkeMuligAGDTO(null)
        forstePeriode.reisetilskudd shouldBeEqualTo false

        val andrePeriode = pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder[1]
        andrePeriode.fom shouldBeEqualTo LocalDate.of(2022, 1, 7)
        andrePeriode.tom shouldBeEqualTo LocalDate.of(2022, 1, 15)
        andrePeriode.varighet shouldBeEqualTo 9
        andrePeriode.gradert shouldBeEqualTo GradertDTO(50, false)
        andrePeriode.behandlingsdager shouldBeEqualTo null
        andrePeriode.innspillTilArbeidsgiver shouldBeEqualTo null
        andrePeriode.type shouldBeEqualTo PeriodetypeDTO.GRADERT
        andrePeriode.aktivitetIkkeMulig shouldBeEqualTo null
        andrePeriode.reisetilskudd shouldBeEqualTo false

        val tredjePeriode = pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.last()
        tredjePeriode.fom shouldBeEqualTo LocalDate.of(2022, 1, 16)
        tredjePeriode.tom shouldBeEqualTo LocalDate.of(2022, 1, 20)
        tredjePeriode.varighet shouldBeEqualTo 5
        tredjePeriode.gradert shouldBeEqualTo null
        tredjePeriode.behandlingsdager shouldBeEqualTo 1
        tredjePeriode.innspillTilArbeidsgiver shouldBeEqualTo null
        tredjePeriode.type shouldBeEqualTo PeriodetypeDTO.BEHANDLINGSDAGER
        tredjePeriode.aktivitetIkkeMulig shouldBeEqualTo null
        tredjePeriode.reisetilskudd shouldBeEqualTo false
    }

    @Test
    internal fun `Mapper sykmelding med en periode riktig`() {
        val sykmeldingId = UUID.randomUUID().toString()
        val perioder =
            listOf(
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 10, 3),
                    tom = LocalDate.of(2022, 11, 6),
                    gradert = null,
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                    aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                    reisetilskudd = false,
                ),
            )
        val sykmeldingKafkaMessage = getSykmeldingKafkaMessage(sykmeldingId, perioder)

        val pdfPayload =
            sykmeldingKafkaMessage.sykmelding.toPdfPayload(
                person,
                narmesteLeder,
                listOf(
                    LocalDate.of(2022, 10, 3),
                    LocalDate.of(2022, 10, 4),
                ),
            )

        pdfPayload.ansatt shouldBeEqualTo Ansatt("fnr", "Per Person")
        pdfPayload.narmesteleder shouldBeEqualTo narmesteLeder
        pdfPayload.arbeidsgiverSykmelding.arbeidsgiverNavn shouldBeEqualTo "ArbeidsgiverNavn"
        pdfPayload.arbeidsgiverSykmelding.prognose shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.prognose
        pdfPayload.arbeidsgiverSykmelding.tiltakArbeidsplassen shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.tiltakArbeidsplassen
        pdfPayload.arbeidsgiverSykmelding.meldingTilArbeidsgiver shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.meldingTilArbeidsgiver
        pdfPayload.arbeidsgiverSykmelding.behandler shouldBeEqualTo
            BehandlerPdf(
                "Behandlerfornavn Behandlermellomnavn Behandleretternavn",
                "telefon",
            )
        pdfPayload.arbeidsgiverSykmelding.egenmeldingsdager?.size shouldBeEqualTo 2
        pdfPayload.arbeidsgiverSykmelding.egenmeldingsdager?.get(0) shouldBeEqualTo
            LocalDate.of(2022, 10, 3)
        pdfPayload.arbeidsgiverSykmelding.egenmeldingsdager?.get(1) shouldBeEqualTo
            LocalDate.of(2022, 10, 4)
        val sykmeldingsperiode = pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.first()
        sykmeldingsperiode.fom shouldBeEqualTo LocalDate.of(2022, 10, 3)
        sykmeldingsperiode.tom shouldBeEqualTo LocalDate.of(2022, 11, 6)
        sykmeldingsperiode.varighet shouldBeEqualTo 35
        sykmeldingsperiode.gradert shouldBeEqualTo null
        sykmeldingsperiode.behandlingsdager shouldBeEqualTo null
        sykmeldingsperiode.innspillTilArbeidsgiver shouldBeEqualTo null
        sykmeldingsperiode.type shouldBeEqualTo PeriodetypeDTO.AKTIVITET_IKKE_MULIG
        sykmeldingsperiode.aktivitetIkkeMulig shouldBeEqualTo AktivitetIkkeMuligAGDTO(null)
        sykmeldingsperiode.reisetilskudd shouldBeEqualTo false
    }

    @Test
    internal fun `Mapper utenlandsk sykmelding riktig`() {
        val sykmeldingId = UUID.randomUUID().toString()
        val perioder =
            listOf(
                SykmeldingsperiodeAGDTO(
                    fom = LocalDate.of(2022, 10, 3),
                    tom = LocalDate.of(2022, 11, 6),
                    gradert = null,
                    behandlingsdager = null,
                    innspillTilArbeidsgiver = null,
                    type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                    aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                    reisetilskudd = false,
                ),
            )
        val sykmeldingKafkaMessage =
            getSykmeldingKafkaMessage(
                sykmeldingId,
                perioder,
                UtenlandskSykmeldingAGDTO("POL"),
            )

        val pdfPayload =
            sykmeldingKafkaMessage.sykmelding.toPdfPayload(
                person,
                narmesteLeder,
                emptyList(),
            )

        pdfPayload.ansatt shouldBeEqualTo Ansatt("fnr", "Per Person")
        pdfPayload.narmesteleder shouldBeEqualTo narmesteLeder
        pdfPayload.arbeidsgiverSykmelding.arbeidsgiverNavn shouldBeEqualTo "ArbeidsgiverNavn"
        pdfPayload.arbeidsgiverSykmelding.prognose shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.prognose
        pdfPayload.arbeidsgiverSykmelding.tiltakArbeidsplassen shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.tiltakArbeidsplassen
        pdfPayload.arbeidsgiverSykmelding.meldingTilArbeidsgiver shouldBeEqualTo
            sykmeldingKafkaMessage.sykmelding.meldingTilArbeidsgiver
        pdfPayload.arbeidsgiverSykmelding.behandler shouldBeEqualTo BehandlerPdf("", null)
        pdfPayload.arbeidsgiverSykmelding.egenmeldingsdager?.size shouldBeEqualTo 0
        val sykmeldingsperiode = pdfPayload.arbeidsgiverSykmelding.sykmeldingsperioder.first()
        sykmeldingsperiode.fom shouldBeEqualTo LocalDate.of(2022, 10, 3)
        sykmeldingsperiode.tom shouldBeEqualTo LocalDate.of(2022, 11, 6)
        sykmeldingsperiode.varighet shouldBeEqualTo 35
        sykmeldingsperiode.gradert shouldBeEqualTo null
        sykmeldingsperiode.behandlingsdager shouldBeEqualTo null
        sykmeldingsperiode.innspillTilArbeidsgiver shouldBeEqualTo null
        sykmeldingsperiode.type shouldBeEqualTo PeriodetypeDTO.AKTIVITET_IKKE_MULIG
        sykmeldingsperiode.aktivitetIkkeMulig shouldBeEqualTo AktivitetIkkeMuligAGDTO(null)
        sykmeldingsperiode.reisetilskudd shouldBeEqualTo false
    }
}
