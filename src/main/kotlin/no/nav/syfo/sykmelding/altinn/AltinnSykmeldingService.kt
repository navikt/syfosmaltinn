package no.nav.syfo.sykmelding.altinn

import no.nav.syfo.log
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class AltinnSykmeldingService {
    fun handleSendtSykmelding(
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        person: Person
    ) {
        val altinnSykmelding = SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(
            sendtSykmeldingKafkaMessage,
            person
        )
        log.info("Mapped sykmelding to Altinn XML format for sykmeldingId ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")
    }
}
