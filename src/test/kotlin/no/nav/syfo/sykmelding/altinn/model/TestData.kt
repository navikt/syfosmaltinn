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
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

fun getSykmeldingKafkaMessage(sykmeldingId: String): SendtSykmeldingKafkaMessage {
    val sendtSykmeldingKafkaMessage = SendtSykmeldingKafkaMessage(
        sykmelding = EnkelSykmelding(
            id = sykmeldingId,
            arbeidsgiver = ArbeidsgiverDTO("ArbeidsgiverNavn", 100),
            behandler = BehandlerDTO(
                "BehandlerFornavn",
                "BehandlerMellomnavn",
                "BehandlerEtternavn",
                "aktorid",
                "12345678901",
                "7898789",
                null,
                AdresseDTO(
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                "Kontaktinformasjon"
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
    return sendtSykmeldingKafkaMessage
}
