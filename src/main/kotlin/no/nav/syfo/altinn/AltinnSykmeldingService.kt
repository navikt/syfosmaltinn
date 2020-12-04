package no.nav.syfo.altinn

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.nav.syfo.Environment
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class AltinnSykmeldingService(private val altinnClient: AltinnClient, private val environment: Environment, private val altinnOrgnummerLookup: AltinnOrgnummerLookup, private val juridiskLoggService: JuridiskLoggService) {
    suspend fun handleSendtSykmelding(
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        pasient: Person,
        narmesteLeder: NarmesteLeder?
    ) {
        val sykmeldingAltinn = SykmeldingAltinn(sendtSykmeldingKafkaMessage, pasient, narmesteLeder)
        val orgnummer = altinnOrgnummerLookup.getOrgnummer(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer)
        val insertCorrespondenceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
            sykmeldingAltinn,
            sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" "),
            orgnummer)
        val sendt = altinnClient.isSendt(sendtSykmeldingKafkaMessage.sykmelding.id, orgnummer)
        when (sendt) {
            false -> sendToAltinn(insertCorrespondenceV2, sendtSykmeldingKafkaMessage, sykmeldingAltinn, pasient)
            true -> {
                log.info("Sykmelding with id ${sendtSykmeldingKafkaMessage.sykmelding.id} is already sendt to Altinn")
            }
        }
    }

    private suspend fun sendToAltinn(
        insertCorrespondenceV2: InsertCorrespondenceV2,
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        sykmeldingAltinn: SykmeldingAltinn,
        pasient: Person
    ) {
        if (environment.cluster == "dev-gcp") {
            log.info("Sending sykmelding with id ${sendtSykmeldingKafkaMessage.sykmelding.id} to Altinn")
            altinnClient.sendToAltinn(insertCorrespondenceV2, sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId)
            juridiskLoggService.sendJuridiskLogg(sykmeldingAltinn, person = pasient)
        }
    }
}
