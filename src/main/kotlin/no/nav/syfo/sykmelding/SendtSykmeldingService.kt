package no.nav.syfo.sykmelding

import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.altinn.AltinnSykmeldingService
import no.nav.syfo.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.log
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.service.BeOmNyNLService
import no.nav.syfo.narmesteleder.service.NarmesteLederService
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.getStatus
import no.nav.syfo.sykmelding.exceptions.ArbeidsgiverNotFoundException
import no.nav.syfo.sykmelding.kafka.aiven.SendtSykmeldingAivenConsumer

@KtorExperimentalAPI
class SendtSykmeldingService(
    private val applicationState: ApplicationState,
    private val altinnSykmeldingService: AltinnSykmeldingService,
    private val pdlClient: PdlClient,
    private val stsTokenClient: StsOidcClient,
    private val narmesteLederService: NarmesteLederService,
    private val beOmNyNLService: BeOmNyNLService,
    private val sendtSykmeldingAivenConsumer: SendtSykmeldingAivenConsumer,
    private val database: DatabaseInterface
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
            handleSendtSykmelding(
                sendtSykmeldingAivenKafkaMessage.kafkaMetadata,
                sendtSykmeldingAivenKafkaMessage.event,
                "aiven"
            ) {
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingAivenKafkaMessage, it)
            }
        }
    }

    private suspend fun handleSendtSykmelding(
        kafkaMetadata: KafkaMetadataDTO,
        event: SykmeldingStatusKafkaEventDTO,
        topic: String,
        xmlSykmeldingArbeidsgiver: (pasient: Person) -> XMLSykmeldingArbeidsgiver
    ) {
        log.info(
            "Mottok sendt sykmelding fra Kafka med sykmeldingId: ${kafkaMetadata.sykmeldingId}, source: ${kafkaMetadata.source} ${
                when (kafkaMetadata.source) {
                    "syfoservice" -> "ignoring"
                    else -> "sending to altinn"
                }
            }"
        )
        if (kafkaMetadata.source == "macgyver") {
            return
        }
        val person = pdlClient.getPerson(
            ident = kafkaMetadata.fnr,
            stsToken = stsTokenClient.oidcToken().access_token
        )
        log.info("Mottok svar fra PDL for sykmeldingId: ${kafkaMetadata.sykmeldingId}")
        val arbeidsgiver = event.arbeidsgiver
            ?: throw ArbeidsgiverNotFoundException(event)

        val narmesteLeder = narmesteLederService.getNarmesteLeder(
            orgnummer = arbeidsgiver.orgnummer,
            fnr = kafkaMetadata.fnr
        )
        log.info("Mottok narmesteleder: ${narmesteLeder != null} for sykmeldingId: ${kafkaMetadata.sykmeldingId}")

        val xmlSykmeldingArbeidsgiver = xmlSykmeldingArbeidsgiver.invoke(person)
        val sykmeldingId = xmlSykmeldingArbeidsgiver.sykmeldingId

        val status = database.getStatus(sykmeldingId)

        if (beOmNyNLService.skalBeOmNyNL(event, narmesteLeder) && status?.altinnTimestamp != null) {
            beOmNyNLService.beOmNyNL(kafkaMetadata, event, person)
        }
        altinnSykmeldingService.handleSendtSykmelding(
            xmlSykmeldingArbeidsgiver,
            person,
            narmesteLeder,
            topic
        )
    }
}
