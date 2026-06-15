package no.nav.syfo.sykmelding

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.nav.syfo.ApplicationState
import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.logger
import no.nav.syfo.narmesteleder.service.BeOmNyNLService
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.exceptions.ArbeidsgiverNotFoundException
import no.nav.syfo.sykmelding.kafka.aiven.SendtSykmeldingAivenConsumer
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import java.time.ZoneId
import java.time.ZonedDateTime

class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val narmesteLederService: NarmesteLederService,
    private val beOmNyNLService: BeOmNyNLService,
    private val sendtSykmeldingAivenConsumer: SendtSykmeldingAivenConsumer,
) {
    private val cutoffDatetime =  ZonedDateTime.of(2026, 6, 15, 12, 0, 0, 0, ZoneId.of("Europe/Oslo")).toOffsetDateTime()

    suspend fun start() {
        logger.info("Starting consumer")
        sendtSykmeldingAivenConsumer.subscribe()

        while (applicationState.ready && OffsetDateTime.now().isBefore(cutoffDatetime)) {
            consumeNewTopic()
        }
    }

    private suspend fun consumeNewTopic() {
        val sykmeldinger = sendtSykmeldingAivenConsumer.poll()
        sykmeldinger.forEach { sendtSykmeldingAivenKafkaMessage ->
            if (OffsetDateTime.now().isBefore(cutoffDatetime)) {
                handleSendtSykmelding(sendtSykmeldingAivenKafkaMessage)
            } else {
                logger.info(
                    "Should not sendt sykmelding to altinn ${sendtSykmeldingAivenKafkaMessage.sykmelding.id}"
                )
            }
        }
    }

    private suspend fun handleSendtSykmelding(
        sendSykmeldingAivenKafkaMessage: SendSykmeldingAivenKafkaMessage,
    ) {
        logger.info(
            "Mottok sendt sykmelding fra Kafka med sykmeldingId: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.sykmeldingId}, source: ${sendSykmeldingAivenKafkaMessage.kafkaMetadata.source}",
        )
        if (sendSykmeldingAivenKafkaMessage.kafkaMetadata.source == "macgyver") {
            return
        }
        val person = pdlClient.getPerson(ident = sendSykmeldingAivenKafkaMessage.kafkaMetadata.fnr)
        logger.info(
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
        logger.info(
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
