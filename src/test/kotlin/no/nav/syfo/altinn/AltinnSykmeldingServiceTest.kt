package no.nav.syfo.altinn

import getSykmeldingKafkaMessage
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.syfo.Environment
import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookup
import no.nav.syfo.juridisklogg.JuridiskLoggService
import no.nav.syfo.pdl.client.model.Person
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnSykmeldingServiceTest : Spek({

    val altinnClient = mockk<AltinnClient>(relaxed = true)
    val env = mockk<Environment>(relaxed = true)
    val altinnOrgnummerLookup = mockk<AltinnOrgnummerLookup>(relaxed = true)
    val juridiskLoggService = mockk<JuridiskLoggService>(relaxed = true)

    val altinnSykmeldingService = AltinnSykmeldingService(altinnClient, env, altinnOrgnummerLookup, juridiskLoggService)

    every { env.cluster } returns "dev-gcp"

    beforeEachTest { clearMocks(altinnClient) }

    describe("Send to altinn") {

        it("Should send to altinn") {

            val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage("2")
            val person = Person("fornavn", "mellomnavn", "etternavn", "1", "2")
            every { altinnClient.isSendt(any(), any()) } returns false
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person, null)
            }

            verify(exactly = 1) { altinnClient.sendToAltinn(any(), any()) }
        }

        it("Should not send to altinn") {
            val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage("2")
            val person = Person("fornavn", "mellomnavn", "etternavn", "1", "2")
            every { altinnClient.isSendt(any(), any()) } returns true
            runBlocking {
                altinnSykmeldingService.handleSendtSykmelding(sendtSykmeldingKafkaMessage, person, null)
            }

            verify(exactly = 0) { altinnClient.sendToAltinn(any(), any()) }
        }
    }
})
