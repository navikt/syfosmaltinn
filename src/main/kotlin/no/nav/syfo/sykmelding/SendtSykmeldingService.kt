package no.nav.syfo.sykmelding

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.service.BeOmNyNLService
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.exceptions.ArbeidsgiverNotFoundException
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

@KtorExperimentalAPI
class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val sendtSykmeldingConsumer: SendtSykmeldingConsumer,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val stsTokenClient: StsOidcClient,
    private val narmesteLederService: NarmesteLederService,
    private val beOmNyNLService: BeOmNyNLService
) {
    suspend fun start() {
        log.info("Starting consumer")
        sendtSykmeldingConsumer.subscribe()
        while (applicationState.ready) {
            val sykmeldinger = sendtSykmeldingConsumer.poll()
            sykmeldinger.forEach {
                handleSendtSykmelding(it)
            }
        }
    }

    private suspend fun handleSendtSykmelding(sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage) {
        log.info("Mottok sendt sykmelding fra Kafka med sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}, source: ${sendtSykmeldingKafkaMessage.kafkaMetadata.source} ${when (sendtSykmeldingKafkaMessage.kafkaMetadata.source) { "syfoservice" -> "ignoring" else -> "sending to altinn"}}")
        if (sendtSykmeldingKafkaMessage.kafkaMetadata.source == "macgyver") {
            return
        }
        val person = pdlClient.getPerson(
                ident = sendtSykmeldingKafkaMessage.kafkaMetadata.fnr,
                stsToken = stsTokenClient.oidcToken().access_token
        )
        log.info("Mottok svar fra PDL for sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")
        val arbeidsgiver = sendtSykmeldingKafkaMessage.event.arbeidsgiver
            ?: throw ArbeidsgiverNotFoundException(sendtSykmeldingKafkaMessage.event)

        val narmesteLeder = narmesteLederService.getNarmesteLeder(arbeidsgiver.orgnummer, person.aktorId)
        log.info("Mottok narmesteleder: ${narmesteLeder != null} for sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")
        if (beOmNyNLService.skalBeOmNyNL(sendtSykmeldingKafkaMessage.event, narmesteLeder)) {
            beOmNyNLService.beOmNyNL(sendtSykmeldingKafkaMessage, person)
        }
        altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person, narmesteLeder)
    }
}
