package no.nav.syfo.narmesteleder.service

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import no.nav.syfo.logger
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.kafka.NLRequestProducer
import no.nav.syfo.narmesteleder.kafka.NLResponseProducer
import no.nav.syfo.narmesteleder.kafka.model.KafkaMetadata
import no.nav.syfo.narmesteleder.kafka.model.NlAvbrutt
import no.nav.syfo.narmesteleder.kafka.model.NlKafkaMetadata
import no.nav.syfo.narmesteleder.kafka.model.NlRequest
import no.nav.syfo.narmesteleder.kafka.model.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.kafka.model.NlResponseKafkaMessage
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.fulltNavn
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.hasCheckedNl
import no.nav.syfo.sykmelding.db.insertNarmestelederCheck

class BeOmNyNLService(
    private val nlRequestProducer: NLRequestProducer,
    private val nlResponseProducer: NLResponseProducer,
    private val database: DatabaseInterface,
) {
    fun beOmNyNL(
        kafkaMetadata: KafkaMetadataDTO,
        event: SykmeldingStatusKafkaEventDTO,
        person: Person
    ) {
        logger.info(
            "Ber om ny nærmeste leder og bryter eksisterende kobling for sykmeldingid {}",
            kafkaMetadata.sykmeldingId,
        )
        nlRequestProducer.send(
            NlRequestKafkaMessage(
                nlRequest =
                    NlRequest(
                        requestId =
                            try {
                                UUID.fromString(kafkaMetadata.sykmeldingId)
                            } catch (e: Exception) {
                                logger.warn(
                                    "Sykmeldingid ${kafkaMetadata.sykmeldingId} er ikke uuid, genererer ny id"
                                )
                                UUID.randomUUID()
                            },
                        sykmeldingId = kafkaMetadata.sykmeldingId,
                        fnr = kafkaMetadata.fnr,
                        orgnr = event.arbeidsgiver!!.orgnummer,
                        name = person.fulltNavn(),
                    ),
                metadata =
                    NlKafkaMetadata(
                        timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                        source = kafkaMetadata.source,
                    ),
            ),
        )
        nlResponseProducer.send(
            NlResponseKafkaMessage(
                kafkaMetadata =
                    KafkaMetadata(
                        timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                        source = "syfosmaltinn",
                    ),
                nlAvbrutt =
                    NlAvbrutt(
                        orgnummer = event.arbeidsgiver.orgnummer,
                        sykmeldtFnr = kafkaMetadata.fnr,
                        aktivTom = OffsetDateTime.now(ZoneOffset.UTC),
                    ),
            ),
        )
        database.insertNarmestelederCheck(
            kafkaMetadata.sykmeldingId,
            OffsetDateTime.now(ZoneOffset.UTC)
        )
    }

    fun skalBeOmNyNL(
        sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO,
        narmesteLeder: NarmesteLeder?,
    ): Boolean {
        val narmestelederCheck =
            database.hasCheckedNl(sykmeldingId = sykmeldingStatusKafkaEventDTO.sykmeldingId)
        if (narmestelederCheck) {
            logger.info(
                "Har allerede bedt om ny NL for ${sykmeldingStatusKafkaEventDTO.sykmeldingId}"
            )
            return false
        }
        val nlSporsmal =
            sykmeldingStatusKafkaEventDTO.sporsmals?.find {
                it.shortName == ShortNameDTO.NY_NARMESTE_LEDER
            }
        return when {
            nlSporsmal?.svar == "NEI" -> {
                false
            }
            narmesteLeder == null -> {
                true
            }
            narmesteLeder.arbeidsgiverForskutterer == null -> {
                true
            }
            else -> {
                nlSporsmal?.svar == "JA"
            }
        }
    }
}
