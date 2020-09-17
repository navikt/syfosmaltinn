package no.nav.syfo.sykmelding.kafka.model

import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.model.SendtSykmelding

data class SendtSykmeldingKafkaMessage(
    val sykmelding: SendtSykmelding,
    val kafkaMetadata: KafkaMetadataDTO,
    val event: SykmeldingStatusKafkaEventDTO
)
