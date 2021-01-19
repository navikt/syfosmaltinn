package no.nav.syfo.narmesteleder.service

import io.ktor.util.KtorExperimentalAPI
import io.mockk.clearAllMocks
import io.mockk.mockk
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.narmesteleder.client.NarmestelederClient
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
object NarmesteLederServiceTest : Spek({
    val narmestelederClient = mockk<NarmestelederClient>()
    val pdlClient = mockk<PdlClient>()
    val stsOidcClient = mockk<StsOidcClient>(relaxed = true)
    val narmesteLederService = NarmesteLederService(narmestelederClient, pdlClient, stsOidcClient)

    beforeEachTest {
        clearAllMocks()
    }

    describe("NarmestelederService - leder får riktig navn") {
        it("Leder med mellomnavn") {
            val person = Person("Fornavn", "Mellomnavn", "Etternavn", "aktørid", "fnr")

            val navn = narmesteLederService.getName(person)

            navn shouldEqual "Fornavn Mellomnavn Etternavn"
        }
        it("Leder uten mellomnavn") {
            val person = Person("Fornavn", null, "Etternavn", "aktørid", "fnr")

            val navn = narmesteLederService.getName(person)

            navn shouldEqual "Fornavn Etternavn"
        }
    }
})
