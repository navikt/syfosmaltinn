package no.nav.syfo.sykmelding.altinn.model

import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverSykmelding
import no.nav.syfo.model.sykmelding.arbeidsgiver.BehandlerAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.PrognoseAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.UtenlandskSykmeldingAGDTO
import no.nav.syfo.model.sykmelding.model.AdresseDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage

val defaultPeriodeliste =
    listOf(
        SykmeldingsperiodeAGDTO(
            LocalDate.of(2016, 12, 7),
            LocalDate.of(2016, 12, 7),
            null,
            null,
            "AvventendeSykmelding",
            PeriodetypeDTO.AVVENTENDE,
            null,
            false,
        ),
    )

fun getSykmeldingKafkaMessage(
    sykmeldingId: String,
    periodeliste: List<SykmeldingsperiodeAGDTO> = defaultPeriodeliste,
    utenlandskSykmelding: UtenlandskSykmeldingAGDTO? = null,
    tiltakArbeidsplassen: String? = "TiltakArbeidsplassen",
): SendSykmeldingAivenKafkaMessage {
    return SendSykmeldingAivenKafkaMessage(
        sykmelding =
            ArbeidsgiverSykmelding(
                id = sykmeldingId,
                arbeidsgiver = ArbeidsgiverAGDTO("ArbeidsgiverNavn", "yrke"),
                behandler =
                    if (utenlandskSykmelding != null) {
                        null
                    } else {
                        BehandlerAGDTO(
                            "BehandlerFornavn",
                            "BehandlerMellomnavn",
                            "BehandlerEtternavn",
                            "aktorid",
                            AdresseDTO(
                                null,
                                null,
                                null,
                                null,
                                null,
                            ),
                            "telefon",
                        )
                    },
                behandletTidspunkt = OffsetDateTime.now(),
                egenmeldt = false,
                harRedusertArbeidsgiverperiode = false,
                kontaktMedPasient =
                    if (utenlandskSykmelding == null) {
                        KontaktMedPasientAGDTO(LocalDate.of(2016, 12, 7))
                    } else {
                        KontaktMedPasientAGDTO(kontaktDato = null)
                    },
                meldingTilArbeidsgiver = "MeldingTilArbeidsgiver",
                mottattTidspunkt = OffsetDateTime.now(),
                papirsykmelding = false,
                prognose =
                    PrognoseAGDTO(
                        arbeidsforEtterPeriode = false,
                        hensynArbeidsplassen = "BeskrivHensynArbeidsplassen",
                    ),
                syketilfelleStartDato = LocalDate.of(2016, 12, 7),
                sykmeldingsperioder = periodeliste,
                tiltakArbeidsplassen = tiltakArbeidsplassen,
                merknader = emptyList(),
                utenlandskSykmelding = utenlandskSykmelding,
                signaturDato = OffsetDateTime.now(),
            ),
        event =
            SykmeldingStatusKafkaEventDTO(
                sykmeldingId = sykmeldingId,
                arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
                sporsmals = emptyList(),
                statusEvent = "SENDT",
                timestamp = OffsetDateTime.now(),
            ),
        kafkaMetadata = KafkaMetadataDTO(sykmeldingId, OffsetDateTime.now(), "fnr", "user"),
    )
}
