package no.nav.syfo.narmesteleder.service

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import no.nav.syfo.log
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

class BeOmNyNLService(private val nlRequestProducer: NLRequestProducer, private val nlResponseProducer: NLResponseProducer) {
    fun beOmNyNL(kafkaMetadata: KafkaMetadataDTO, event: SykmeldingStatusKafkaEventDTO, person: Person) {
        if (kafkaMetadata.source == "user") {
            log.info("Ber om ny nærmeste leder og bryter eksisterende kobling for sykmeldingid {}", kafkaMetadata.sykmeldingId)
            nlRequestProducer.send(
                NlRequestKafkaMessage(
                    nlRequest = NlRequest(
                        requestId = UUID.fromString(kafkaMetadata.sykmeldingId),
                        sykmeldingId = kafkaMetadata.sykmeldingId,
                        fnr = kafkaMetadata.fnr,
                        orgnr = event.arbeidsgiver!!.orgnummer,
                        name = person.fulltNavn()
                    ),
                    metadata = NlKafkaMetadata(
                        timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                        source = kafkaMetadata.source
                    )
                )
            )
            nlResponseProducer.send(
                NlResponseKafkaMessage(
                    kafkaMetadata = KafkaMetadata(
                        timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                        source = "syfosmaltinn"
                    ),
                    nlAvbrutt = NlAvbrutt(
                        orgnummer = event.arbeidsgiver!!.orgnummer,
                        sykmeldtFnr = kafkaMetadata.fnr,
                        aktivTom = OffsetDateTime.now(ZoneOffset.UTC)
                    )
                )
            )
        }
    }

    fun skalBeOmNyNL(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, narmesteLeder: NarmesteLeder?): Boolean {
        val nlSporsmal = sykmeldingStatusKafkaEventDTO.sporsmals?.find { it.shortName == ShortNameDTO.NY_NARMESTE_LEDER }
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
