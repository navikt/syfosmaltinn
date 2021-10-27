
import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverSykmelding
import no.nav.syfo.model.sykmelding.arbeidsgiver.BehandlerAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.PrognoseAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.model.AdresseDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage

fun getSykmeldingKafkaMessage(sykmeldingId: String): SendSykmeldingAivenKafkaMessage {
    val sendtSykmeldingKafkaMessage = SendSykmeldingAivenKafkaMessage(
        sykmelding = ArbeidsgiverSykmelding(
            id = sykmeldingId,
            arbeidsgiver = ArbeidsgiverAGDTO("ArbeidsgiverNavn", "yrke"),
            behandler = BehandlerAGDTO(
                "BehandlerFornavn",
                "BehandlerMellomnavn",
                "BehandlerEtternavn",
                "aktorid",
                AdresseDTO(
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                "telefon"
            ),
            behandletTidspunkt = OffsetDateTime.now(),
            egenmeldt = false,
            harRedusertArbeidsgiverperiode = false,
            kontaktMedPasient = KontaktMedPasientAGDTO(LocalDate.of(2016, 12, 7)),
            meldingTilArbeidsgiver = "MeldingTilArbeidsgiver",
            mottattTidspunkt = OffsetDateTime.now(),
            papirsykmelding = false,
            prognose = PrognoseAGDTO(
                arbeidsforEtterPeriode = false,
                hensynArbeidsplassen = "BeskrivHensynArbeidsplassen"
            ),
            syketilfelleStartDato = LocalDate.of(2016, 12, 7),
            sykmeldingsperioder = listOf(
                SykmeldingsperiodeAGDTO(
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
            tiltakArbeidsplassen = "TiltakArbeidsplassen",
            merknader = emptyList()
        ),
        event = SykmeldingStatusKafkaEventDTO(
            sykmeldingId = sykmeldingId,
            arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
            sporsmals = emptyList(),
            statusEvent = "SENDT",
            timestamp = OffsetDateTime.now()
        ),
        kafkaMetadata = KafkaMetadataDTO(sykmeldingId, OffsetDateTime.now(), "fnr", "user")
    )
    return sendtSykmeldingKafkaMessage
}
