package no.nav.syfo.altinn

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.altinn.pdf.PdfgenClient
import no.nav.syfo.altinn.pdf.toPdfPayload
import no.nav.syfo.application.metrics.ALTINN_COUNTER
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.fulltNavn
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.SykmeldingStatus
import no.nav.syfo.sykmelding.db.getStatus
import no.nav.syfo.sykmelding.db.insertStatus
import no.nav.syfo.sykmelding.db.updateSendtToAlinn
import no.nav.syfo.sykmelding.db.updateSendtToLogg
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import java.time.OffsetDateTime
import java.time.ZoneOffset

class AltinnSykmeldingService(
    private val altinnClient: AltinnClient,
    private val altinnOrgnummerLookup: AltinnOrgnummerLookup,
    private val juridiskLoggService: JuridiskLoggService,
    private val database: DatabaseInterface,
    private val pdfgenClient: PdfgenClient
) {

    suspend fun handleSendtSykmelding(
        sendSykmeldingAivenKafkaMessage: SendSykmeldingAivenKafkaMessage,
        pasient: Person,
        narmesteLeder: NarmesteLeder?
    ) {
        val xmlSykmeldingArbeidsgiver = SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendSykmeldingAivenKafkaMessage, pasient)
        val pdf = pdfgenClient.createPdf(sendSykmeldingAivenKafkaMessage.sykmelding.toPdfPayload(pasient, narmesteLeder))
        val sykmeldingAltinn = SykmeldingAltinn(xmlSykmeldingArbeidsgiver, narmesteLeder, pdf)
        val orgnummer = altinnOrgnummerLookup.getOrgnummer(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer)
        val sykmeldingId = xmlSykmeldingArbeidsgiver.sykmeldingId

        val insertCorrespondenceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
            sykmeldingAltinn,
            pasient.fulltNavn(),
            orgnummer
        )

        val status = database.getStatus(sykmeldingId)

        if (status == null) {
            database.insertStatus(sykmeldingId)
        }
        sendtToAltinn(status, insertCorrespondenceV2, orgnummer, sykmeldingId)
        sendtToLogg(sykmeldingAltinn, status)
    }

    private fun sendtToAltinn(
        status: SykmeldingStatus?,
        insertCorrespondenceV2: InsertCorrespondenceV2,
        orgnummer: String,
        sykmeldingId: String
    ) {
        when {
            status == null -> {
                sendToAltinn(insertCorrespondenceV2, sykmeldingId)
                database.updateSendtToAlinn(sykmeldingId, OffsetDateTime.now(ZoneOffset.UTC))
            }
            status.altinnTimestamp == null -> {
                when (altinnClient.isSendt(status.sykmeldingId, orgnummer)) {
                    false -> sendToAltinn(
                        insertCorrespondenceV2,
                        sykmeldingId
                    )
                    true -> log.info("Sykmelding already sendt to altinn")
                }
                database.updateSendtToAlinn(sykmeldingId, OffsetDateTime.now(ZoneOffset.UTC))
            }
            else -> {
                log.info("Sykmelding already sendt to altinn")
            }
        }
    }

    private fun sendToAltinn(
        insertCorrespondenceV2: InsertCorrespondenceV2,
        sykmeldingId: String
    ) {
        log.info("Sending sykmelding with id $sykmeldingId to Altinn")
        altinnClient.sendToAltinn(insertCorrespondenceV2, sykmeldingId)
        ALTINN_COUNTER.inc()
    }

    private fun sendtToLogg(
        sykmeldingAltinn: SykmeldingAltinn,
        status: SykmeldingStatus?
    ) {
        when (status?.loggTimestamp) {
            null -> {
                juridiskLoggService.sendJuridiskLogg(sykmeldingAltinn, sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId)
                database.updateSendtToLogg(
                    sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId,
                    OffsetDateTime.now(ZoneOffset.UTC)
                )
            }
            else -> {
                log.info("Sykmelding ${sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId} already sendt to juridisk logg")
            }
        }
    }
}
