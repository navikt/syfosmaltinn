package no.nav.syfo.sykmelding

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.sykmelding.altinn.AltinnSykmeldingService
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val sendtSykmeldingConsumer: SendtSykmeldingConsumer,
    private val altinnSykmeldingService: AltinnSykmeldingService
) {
    suspend fun start() {
        while (applicationState.ready) {
            val sykmeldinger = sendtSykmeldingConsumer.poll()
            sykmeldinger.forEach {
                handleSendtSykmelding(it)
            }
        }
    }

    private fun handleSendtSykmelding(it: SendtSykmeldingKafkaMessage) {
    }
}
