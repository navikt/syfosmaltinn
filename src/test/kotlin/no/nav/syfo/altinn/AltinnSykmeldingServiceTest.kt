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
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.SykmeldingStatus
import no.nav.syfo.sykmelding.db.getStatus
import org.junit.Test
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnSykmeldingServiceTest : Spek({

    val altinnClient = mockk<AltinnClient>(relaxed = true)
    val env = mockk<Environment>(relaxed = true)
    val altinnOrgnummerLookup = mockk<AltinnOrgnummerLookup>(relaxed = true)
    val juridiskLoggService = mockk<JuridiskLoggService>(relaxed = true)
    val database = mockk<DatabaseInterface>(relaxed = true)
    val altinnSykmeldingService = AltinnSykmeldingService(altinnClient, env, altinnOrgnummerLookup, juridiskLoggService, database)

    beforeEachTest {
        clearAllMocks()
        mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
        every { env.cluster } returns "dev-gcp"
    }

    describe("Send to altinn") {

        it("Should send to altinn") {

            val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage("2")
            val person = Person("fornavn", "mellomnavn", "etternavn", "1", "2")
            every { altinnClient.isSendt(any(), any()) } returns false
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person, null)
            }

            verify(exactly = 1) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 1) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
        }

        it("Should not send to altinn or logg") {
            val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage("2")
            val person = Person("fornavn", "mellomnavn", "etternavn", "1", "2")
            every { database.getStatus(any()) } returns SykmeldingStatus("123", OffsetDateTime.now(), OffsetDateTime.now())
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person, null)
            }

            verify(exactly = 0) { altinnClient.sendToAltinn(any(), any()) }
            coVerify(exactly = 0) { juridiskLoggService.sendJuridiskLogg(any(), any()) }
        }
    }
})

class Runner() {
    @Test
    fun runner() {
    }
}
