package no.nav.syfo.sykmelding

import no.nav.syfo.application.ApplicationState
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.NarmestelederClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.sykmelding.altinn.AltinnSykmeldingService
import no.nav.syfo.sykmelding.exceptions.ArbeidsgiverNotFoundException
import no.nav.syfo.sykmelding.kafka.SendtSykmeldingConsumer
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val sendtSykmeldingConsumer: SendtSykmeldingConsumer,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val stsTokenClient: StsOidcClient,
    private val narmestelederClient: NarmestelederClient
) {
    suspend fun start() {
        sendtSykmeldingConsumer.subscribe()
        while (applicationState.ready) {
            val sykmeldinger = sendtSykmeldingConsumer.poll()
            sykmeldinger.forEach {
                handleSendtSykmelding(it)
            }
        }
    }

    private suspend fun handleSendtSykmelding(sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage) {
        log.info("Mottok sendt sykmelding fra Kafka med sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")

        val person = pdlClient.getPerson(
                fnr = sendtSykmeldingKafkaMessage.kafkaMetadata.fnr,
                stsToken = stsTokenClient.oidcToken().access_token,
                sykmeldingId = sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId
        )
        log.info("Mottok svar fra PDL for sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")

        if (sendtSykmeldingKafkaMessage.event.arbeidsgiver == null) {
            throw ArbeidsgiverNotFoundException(sendtSykmeldingKafkaMessage.event)
        }
        val arbeidsgiver = sendtSykmeldingKafkaMessage.event.arbeidsgiver ?: throw RuntimeException("")
        val aktorId = person.aktorId
        val narmesteleder = narmestelederClient.getNarmesteleder(arbeidsgiver.orgnummer, aktorId)
        log.info("Mottok narmesteleder: ${narmesteleder.narmesteLederRelasjon == null} for sykmeldingId: ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")

        altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person)
    }
}
