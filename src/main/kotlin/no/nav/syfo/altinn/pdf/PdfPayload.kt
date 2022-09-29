package no.nav.syfo.altinn.pdf

import no.nav.syfo.model.sykmelding.arbeidsgiver.AktivitetIkkeMuligAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.PrognoseAGDTO
import no.nav.syfo.model.sykmelding.model.GradertDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import java.time.LocalDate
import java.time.OffsetDateTime

data class PdfPayload(
    val ansatt: Ansatt,
    val narmesteleder: NarmesteLeder?,
    val arbeidsgiverSykmelding: ArbeidsgiverSykmeldingPdf
)

data class Ansatt(
    val fnr: String,
    val navn: String
)

data class ArbeidsgiverSykmeldingPdf(
    val id: String,
    val syketilfelleStartDato: LocalDate?,
    val behandletTidspunkt: OffsetDateTime,
    val arbeidsgiverNavn: String?,
    val sykmeldingsperioder: List<SykmeldingsperiodePdf>,
    val prognose: PrognoseAGDTO?,
    val tiltakArbeidsplassen: String?,
    val meldingTilArbeidsgiver: String?,
    val behandler: BehandlerPdf
)

data class SykmeldingsperiodePdf(
    val fom: LocalDate,
    val tom: LocalDate,
    val varighet: Int,
    val gradert: GradertDTO?,
    val behandlingsdager: Int?,
    val innspillTilArbeidsgiver: String?,
    val type: PeriodetypeDTO,
    val aktivitetIkkeMulig: AktivitetIkkeMuligAGDTO?,
    val reisetilskudd: Boolean
)

data class BehandlerPdf(
    val navn: String,
    val tlf: String?
)
