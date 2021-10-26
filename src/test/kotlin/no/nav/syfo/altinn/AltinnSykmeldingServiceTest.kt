package no.nav.syfo.altinn

import getSykmeldingKafkaMessage
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking
import no.nav.syfo.Environment
import no.nav.syfo.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.SykmeldingStatus
import no.nav.syfo.sykmelding.db.getStatus
import no.nav.syfo.sykmelding.db.insertStatus
import no.nav.syfo.sykmelding.db.updateSendtToAlinn
import no.nav.syfo.sykmelding.db.updateSendtToLogg
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnSykmeldingServiceTest : Spek({

    val altinnClient = mockk<AltinnClient>(relaxed = true)
    val env = mockk<Environment>(relaxed = true)
    val altinnOrgnummerLookup = mockk<AltinnOrgnummerLookup>(relaxed = true)
    val juridiskLoggService = mockk<JuridiskLoggService>(relaxed = true)
    val database = mockk<DatabaseInterface>(relaxed = true)
    val altinnSykmeldingService = AltinnSykmeldingService(
        altinnClient,
        altinnOrgnummerLookup,
        juridiskLoggService,
        database
    )

    beforeEachTest {
        clearAllMocks()
        mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
        every { env.cluster } returns "dev-gcp"
    }

    describe("Send to altinn") {
        val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage("2")
        val person = Person("fornavn", "mellomnavn", "etternavn", "1", "2")

        it("Should send to altinn") {
            every { database.getStatus(any()) } returns null
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, person), person, null, "topic")
            }
            verify(exactly = 1) { database.insertStatus(any()) }
            verify(exactly = 1) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 1) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
        }

        it("Should not send to altinn or logg") {

            every { database.getStatus(any()) } returns SykmeldingStatus("123", OffsetDateTime.now(), OffsetDateTime.now())
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, person), person, null, "topic")
            }
            verify(exactly = 0) { database.insertStatus(any()) }
            verify(exactly = 0) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 0) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
        }

        it("Should sendt to altinn when timestamp is null and is not sendt to altinn") {
            every { database.getStatus(any()) } returns SykmeldingStatus("123", null, null)
            every { altinnClient.isSendt(any(), any()) } returns false
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, person), person, null, "topic")
            }
            verify(exactly = 1) { altinnClient.isSendt(any(), any()) }
            verify(exactly = 1) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 1) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
            verify(exactly = 0) { database.insertStatus(any()) }
            verify(exactly = 1) { database.updateSendtToAlinn(any(), any()) }
            verify(exactly = 1) { database.updateSendtToLogg(any(), any()) }
        }

        it("Should not sendt to altinn when timestamp is null and is sendt to altinn") {
            every { database.getStatus(any()) } returns SykmeldingStatus("123", null, null)
            every { altinnClient.isSendt(any(), any()) } returns true
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, person), person, null, "topic")
            }
            verify(exactly = 1) { altinnClient.isSendt(any(), any()) }
            verify(exactly = 0) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 1) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
            verify(exactly = 0) { database.insertStatus(any()) }
            verify(exactly = 1) { database.updateSendtToAlinn(any(), any()) }
            verify(exactly = 1) { database.updateSendtToLogg(any(), any()) }
        }

        it("send to juridisk logg when timestamp is null") {
            every { database.getStatus(any()) } returns SykmeldingStatus("123", OffsetDateTime.now(), null)
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, person), person, null, "topic")
            }
            verify(exactly = 0) { altinnClient.isSendt(any(), any()) }
            verify(exactly = 0) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 1) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
            verify(exactly = 0) { database.insertStatus(any()) }
            verify(exactly = 0) { database.updateSendtToAlinn(any(), any()) }
            verify(exactly = 1) { database.updateSendtToLogg(any(), any()) }
        }
    }
})
