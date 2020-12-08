package no.nav.syfo.altinn

import java.time.OffsetDateTime
import java.time.ZoneOffset
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.nav.syfo.Environment
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.SykmeldingStatus
import no.nav.syfo.sykmelding.db.getStatus
import no.nav.syfo.sykmelding.db.insertStatus
import no.nav.syfo.sykmelding.db.updateSendtToAlinn
import no.nav.syfo.sykmelding.db.updateSendtToLogg
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class AltinnSykmeldingService(private val altinnClient: AltinnClient, private val environment: Environment, private val altinnOrgnummerLookup: AltinnOrgnummerLookup, private val juridiskLoggService: JuridiskLoggService, private val database: DatabaseInterface) {
    suspend fun handleSendtSykmelding(
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        pasient: Person,
        narmesteLeder: NarmesteLeder?
    ) {

        val sykmeldingAltinn = SykmeldingAltinn(sendtSykmeldingKafkaMessage, pasient, narmesteLeder)
        val orgnummer = altinnOrgnummerLookup.getOrgnummer(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer)
        val sykmeldingId = sendtSykmeldingKafkaMessage.sykmelding.id

        val insertCorrespondenceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
            sykmeldingAltinn,
            sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" "),
            orgnummer)

        val status = database.getStatus(sykmeldingId)

        if (status == null) {
            database.insertStatus(sykmeldingId)
        }
        sendtToAltinn(status, insertCorrespondenceV2, sendtSykmeldingKafkaMessage, orgnummer, sykmeldingId)
        sendtToLogg(sykmeldingAltinn, pasient, status)
    }

    private fun sendtToAltinn(
        status: SykmeldingStatus?,
        insertCorrespondenceV2: InsertCorrespondenceV2,
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
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
                database.updateSendtToAlinn(sendtSykmeldingKafkaMessage.sykmelding.id, OffsetDateTime.now(ZoneOffset.UTC))
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
    }

    private suspend fun sendtToLogg(sykmeldingAltinn: SykmeldingAltinn, pasient: Person, status: SykmeldingStatus?) {
        when (status?.loggTimestamp) {
            null -> {
                juridiskLoggService.sendJuridiskLogg(sykmeldingAltinn, person = pasient)
                database.updateSendtToLogg(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId, OffsetDateTime.now(ZoneOffset.UTC))
            }
            else -> {
                log.info("Sykmelding already sendt to juridisk logg")
            }
        }
    }
}
