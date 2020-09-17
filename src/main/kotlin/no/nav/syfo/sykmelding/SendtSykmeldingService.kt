package no.nav.syfo.sykmelding

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.altinn.AltinnSykmeldingService
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val sendtSykmeldingConsumer: SendtSykmeldingConsumer,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val stsTokenClient: StsOidcClient
) {
    suspend fun start() {
        while (applicationState.ready) {
            val sykmeldinger = sendtSykmeldingConsumer.poll()
            sykmeldinger.forEach {
                handleSendtSykmelding(it)
            }
        }
    }

    private suspend fun handleSendtSykmelding(it: SendtSykmeldingKafkaMessage) {
        val navn = pdlClient.getNavn(
            fnr = it.kafkaMetadata.fnr,
            stsToken = stsTokenClient.oidcToken().access_token,
            sykmeldingId = it.kafkaMetadata.sykmeldingId
        )
        altinnSykmeldingService.handleSendtSykmelding(it, navn)
    }
}
