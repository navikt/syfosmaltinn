package no.nav.syfo.altinn.pdf

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverSykmelding
import no.nav.syfo.model.sykmelding.arbeidsgiver.BehandlerAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.capitalizeFirstLetter
import no.nav.syfo.pdl.client.model.fulltNavn

fun ArbeidsgiverSykmelding.toPdfPayload(
    pasient: Person,
    narmesteLeder: NarmesteLeder?,
    egenmeldingsdager: List<LocalDate>?,
): PdfPayload {
    return PdfPayload(
        ansatt =
            Ansatt(
                fnr = pasient.fnr,
                navn = pasient.fulltNavn(),
            ),
        narmesteleder = narmesteLeder,
        arbeidsgiverSykmelding =
            ArbeidsgiverSykmeldingPdf(
                id = id,
                syketilfelleStartDato = syketilfelleStartDato,
                behandletTidspunkt = behandletTidspunkt,
                arbeidsgiverNavn = arbeidsgiver.navn,
                sykmeldingsperioder =
                    sykmeldingsperioder.map { it.toSykmeldingsPeriodePdf() }.sortedBy { it.fom },
                prognose = prognose,
                tiltakArbeidsplassen =
                    tiltakArbeidsplassenWithoutIllegalCharacters(tiltakArbeidsplassen),
                meldingTilArbeidsgiver = meldingTilArbeidsgiver,
                behandler =
                    BehandlerPdf(
                        navn = behandler?.getFormattertNavn() ?: "",
                        tlf = behandler?.tlf,
                    ),
                egenmeldingsdager = egenmeldingsdager,
                kontaktMedPasient =
                    KontaktMedPasientAGDTO(kontaktDato = kontaktMedPasient.kontaktDato)
            ),
    )
}

private fun tiltakArbeidsplassenWithoutIllegalCharacters(tiltakArbeidsplassen: String?): String? {
    return if (tiltakArbeidsplassen.isNullOrEmpty()) {
        tiltakArbeidsplassen
    } else {
        tiltakArbeidsplassen.replace(regex = Regex("\\p{C}"), "")
    }
}

private fun SykmeldingsperiodeAGDTO.toSykmeldingsPeriodePdf(): SykmeldingsperiodePdf {
    return SykmeldingsperiodePdf(
        fom = fom,
        tom = tom,
        varighet = (fom..tom).daysBetween().toInt() + 1,
        gradert = gradert,
        behandlingsdager = behandlingsdager,
        innspillTilArbeidsgiver = innspillTilArbeidsgiver,
        type = type,
        aktivitetIkkeMulig = aktivitetIkkeMulig,
        reisetilskudd = reisetilskudd,
    )
}

fun ClosedRange<LocalDate>.daysBetween(): Long = ChronoUnit.DAYS.between(start, endInclusive)

private fun BehandlerAGDTO.getFormattertNavn(): String {
    return if (mellomnavn.isNullOrEmpty()) {
        capitalizeFirstLetter("$fornavn $etternavn")
    } else {
        capitalizeFirstLetter("$fornavn $mellomnavn $etternavn")
    }
}
