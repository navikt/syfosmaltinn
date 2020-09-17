package no.nav.syfo.sykmelding.altinn

import no.nav.syfo.pdl.client.model.Navn
import no.nav.syfo.sykmelding.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class AltinnSykmeldingService {
    fun handleSendtSykmelding(
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        navn: Navn
    ) {
        val altinnSykmelding = AltinnSykmeldingMapper.toAltinnXMLSykmelding(
            sendtSykmeldingKafkaMessage,
            navn
        )
    }
}
