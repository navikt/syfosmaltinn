package no.nav.syfo.narmesteleder.service

import java.time.OffsetDateTime
import java.util.UUID
import no.nav.syfo.log
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.kafka.NLRequestProducer
import no.nav.syfo.narmesteleder.kafka.model.NlKafkaMetadata
import no.nav.syfo.narmesteleder.kafka.model.NlRequest
import no.nav.syfo.narmesteleder.kafka.model.NlRequestKafkaMessage
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.fulltNavn
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class BeOmNyNLService(private val nlRequestProducer: NLRequestProducer) {
    fun beOmNyNL(sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage, person: Person) {
        if (sendtSykmeldingKafkaMessage.kafkaMetadata.source == "user") {
            log.info("Ber om ny nærmeste leder for sykmeldingid {}", sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId)
            nlRequestProducer.send(
                NlRequestKafkaMessage(
                    nlRequest = NlRequest(
                        requestId = UUID.fromString(sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId),
                        sykmeldingId = sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId,
                        fnr = sendtSykmeldingKafkaMessage.kafkaMetadata.fnr,
                        orgnr = sendtSykmeldingKafkaMessage.event.arbeidsgiver!!.orgnummer,
                        name = person.fulltNavn()
                    ),
                    metadata = NlKafkaMetadata(
                        timestamp = OffsetDateTime.now(),
                        source = sendtSykmeldingKafkaMessage.kafkaMetadata.source
                    )
                )
            )
        }
    }

    fun skalBeOmNyNL(sykmeldingStatusKafkaEventDTO: SykmeldingStatusKafkaEventDTO, narmesteLeder: NarmesteLeder?): Boolean {
        return when {
            narmesteLeder == null -> {
                true
            }
            narmesteLeder.arbeidsgiverForskutterer == null -> {
                true
            }
            else -> {
                val nlSporsmal = sykmeldingStatusKafkaEventDTO.sporsmals?.find { it.shortName == ShortNameDTO.NY_NARMESTE_LEDER }
                nlSporsmal?.svar == "JA"
            }
        }
    }
}
