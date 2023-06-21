package no.nav.syfo.sykmelding

import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.service.BeOmNyNLService
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.exceptions.ArbeidsgiverNotFoundException
import no.nav.syfo.sykmelding.kafka.aiven.SendtSykmeldingAivenConsumer
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage

class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val narmesteLederService: NarmesteLederService,
    private val beOmNyNLService: BeOmNyNLService,
    private val sendtSykmeldingAivenConsumer: SendtSykmeldingAivenConsumer,
) {
    suspend fun start() {
        log.info("Starting consumer")
        sendtSykmeldingAivenConsumer.subscribe()
        while (applicationState.ready) {
            consumeNewTopic()
        }
    }

    private suspend fun consumeNewTopic() {
        val sykmeldinger = sendtSykmeldingAivenConsumer.poll()
        sykmeldinger.forEach { sendtSykmeldingAivenKafkaMessage ->
            handleSendtSykmelding(sendtSykmeldingAivenKafkaMessage)
        }
    }

    private suspend fun handleSendtSykmelding(
        sendSykmeldingAivenKafkaMessage: SendSykmeldingAivenKafkaMessage,
    ) {
        log.info(
            "Mottok sendt sykmelding fra Kafka med sykmeldingId: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.sykmeldingId}, source: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.source}",
        )
        if (sendSykmeldingAivenKafkaMessage.kafkaMetadata.source == "macgyver") {
            return
        }
        val person = pdlClient.getPerson(ident = sendSykmeldingAivenKafkaMessage.kafkaMetadata.fnr)
        log.info(
            "Mottok svar fra PDL for sykmeldingId: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.sykmeldingId}"
        )
        val arbeidsgiver =
            sendSykmeldingAivenKafkaMessage.event.arbeidsgiver
                ?: throw ArbeidsgiverNotFoundException(sendSykmeldingAivenKafkaMessage.event)

        val narmesteLeder =
            narmesteLederService.getNarmesteLeder(
                orgnummer = arbeidsgiver.orgnummer,
                fnr = sendSykmeldingAivenKafkaMessage.kafkaMetadata.fnr,
            )
        log.info(
            "Mottok narmesteleder: ${narmesteLeder != null} for sykmeldingId: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.sykmeldingId}"
        )
        if (beOmNyNLService.skalBeOmNyNL(sendSykmeldingAivenKafkaMessage.event, narmesteLeder)) {
            beOmNyNLService.beOmNyNL(
                sendSykmeldingAivenKafkaMessage.kafkaMetadata,
                sendSykmeldingAivenKafkaMessage.event,
                person
            )
        }
        altinnSykmeldingService.handleSendtSykmelding(
            sendSykmeldingAivenKafkaMessage,
            person,
            narmesteLeder,
        )
    }
}
