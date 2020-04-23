package no.nav.syfo.sykmelding.model

import java.time.LocalDate
import java.time.OffsetDateTime

data class SendtSykmelding(
    val id: String,
    val mottattTidspunkt: OffsetDateTime,
    val legekontorOrgnr: String?,
    val behandletTidspunkt: OffsetDateTime,
    val meldingTilArbeidsgiver: String?,
    val navnFastlege: String?,
    val tiltakArbeidsplassen: String?,
    val syketilfelleStartDato: LocalDate?,
    val behandler: BehandlerDTO,
    val sykmeldingsperioder: List<SykmeldingsperiodeDTO>,
    val arbeidsgiver: ArbeidsgiverDTO,
    val kontaktMedPasient: KontaktMedPasientDTO,
    val prognose: PrognoseDTO?,
    val utdypendeOpplysninger: Map<String, Map<String, SporsmalSvarDTO>>,
    val egenmeldt: Boolean,
    val papirsykmelding: Boolean,
    val harRedusertArbeidsgiverperiode: Boolean
)
