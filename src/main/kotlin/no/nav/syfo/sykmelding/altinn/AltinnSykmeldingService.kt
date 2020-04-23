package no.nav.syfo.sykmelding.altinn

import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.sykmelding.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.sykmelding.model.SendtSykmelding

class AltinnSykmeldingService {
    fun handleSendtSykmelding(sendtSykmelding: SendtSykmelding, sykmeldingSendtEventDTO: SykmeldingStatusKafkaEventDTO) {
        val altinnSykmelding = AltinnSykmeldingMapper.toAltinnXMLSykmelding(sendtSykmelding, sykmeldingSendtEventDTO)
    }
}
