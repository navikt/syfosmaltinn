package no.nav.syfo.altinn

import no.nav.syfo.Environment
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.reportee.AltinnReporteeLookup
import no.nav.syfo.log
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage

class AltinnSykmeldingService(private val altinnClient: AltinnClient, private val environment: Environment, private val altinnReporteeLookup: AltinnReporteeLookup) {
    fun handleSendtSykmelding(
        sendtSykmeldingKafkaMessage: SendtSykmeldingKafkaMessage,
        pasient: Person,
        narmesteLeder: NarmesteLeder?
    ) {
        val sykmeldingAltinn = SykmeldingAltinn(sendtSykmeldingKafkaMessage, pasient, narmesteLeder)
        val insertCorrespondenceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
            sykmeldingAltinn,
            sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" "),
            altinnReporteeLookup.getReportee(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer))
        log.info("Mapped sykmelding to Altinn XML format for sykmeldingId ${sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId}")
        if (environment.cluster == "dev-fss") {
            log.info("Sending to altinn")
            altinnClient.sendToAltinn(insertCorrespondenceV2, sendtSykmeldingKafkaMessage.kafkaMetadata.sykmeldingId)
        }
    }
}
