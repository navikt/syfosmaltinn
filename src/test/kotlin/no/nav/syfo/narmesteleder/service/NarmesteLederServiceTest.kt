package no.nav.syfo.narmesteleder.service

import io.ktor.util.KtorExperimentalAPI
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.db.NarmestelederDbModel
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@KtorExperimentalAPI
object NarmesteLederServiceTest : Spek({
    val narmestelederDb = mockk<NarmestelederDB>()
    val pdlClient = mockk<PdlClient>()
    val stsOidcClient = mockk<StsOidcClient>(relaxed = true)
    val narmesteLederService = NarmesteLederService(narmestelederDb, pdlClient, stsOidcClient)

    beforeEachTest {
        clearAllMocks()
        coEvery { stsOidcClient.oidcToken().access_token } returns "access_token"
    }
    val leder = Person("Fornavn", "Mellomnavn", "Etternavn", "aktørid", "lederFnr")
    describe("NarmestelederService - leder får riktig navn") {

        it("Should get correct NL") {
            every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns NarmestelederDbModel(
                sykmeldtFnr = "1",
                lederFnr = "lederFnr",
                orgnummer = "orgnummer",
                narmesteLederTelefonnummer = "telefon",
                narmesteLederEpost = "epost",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = true
            )

            coEvery { pdlClient.getPerson("lederFnr", "access_token") } returns leder

            val nl = runBlocking { narmesteLederService.getNarmesteLeder(orgnummer = "orgnummer", fnr = "1") }
            nl shouldEqual NarmesteLeder("epost", "orgnummer", "telefon", LocalDate.of(2021, 1, 1), true, "Fornavn Mellomnavn Etternavn", "lederFnr")
        }

        it("Should get null") {
            every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns null
            coEvery { pdlClient.getPerson("lederFnr", "access_token") } returns leder
            val nl = runBlocking { narmesteLederService.getNarmesteLeder("orgnummer", "1") }
            nl shouldEqual null
        }
        it("Leder med mellomnavn") {
            val navn = narmesteLederService.getName(leder)
            navn shouldEqual "Fornavn Mellomnavn Etternavn"
        }
        it("Leder uten mellomnavn") {
            val person = leder.copy(mellomnavn = null)
            val navn = narmesteLederService.getName(person)
            navn shouldEqual "Fornavn Etternavn"
        }
    }
})
