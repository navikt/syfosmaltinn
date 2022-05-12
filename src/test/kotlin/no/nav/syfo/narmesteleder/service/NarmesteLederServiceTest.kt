package no.nav.syfo.narmesteleder.service

import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.db.NarmestelederDbModel
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldBeEqualTo
import java.time.LocalDate

class NarmesteLederServiceTest : FunSpec({
    val narmestelederDb = mockk<NarmestelederDB>()
    val pdlClient = mockk<PdlClient>()
    val narmesteLederService = NarmesteLederService(narmestelederDb, pdlClient)

    beforeTest {
        clearAllMocks()
    }
    val leder = Person("Fornavn", "Mellomnavn", "Etternavn", "aktørid", "lederFnr")
    context("NarmestelederService - leder får riktig navn") {

        test("Should get correct NL") {
            every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns NarmestelederDbModel(
                sykmeldtFnr = "1",
                lederFnr = "lederFnr",
                orgnummer = "orgnummer",
                narmesteLederTelefonnummer = "telefon",
                narmesteLederEpost = "epost",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = true
            )

            coEvery { pdlClient.getPerson("lederFnr") } returns leder

            val nl = narmesteLederService.getNarmesteLeder(orgnummer = "orgnummer", fnr = "1")
            nl shouldBeEqualTo NarmesteLeder(
                "epost",
                "orgnummer",
                "telefon",
                LocalDate.of(2021, 1, 1),
                true,
                "Fornavn Mellomnavn Etternavn",
                "lederFnr"
            )
        }

        test("Should get null") {
            every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns null
            coEvery { pdlClient.getPerson("lederFnr") } returns leder
            val nl = narmesteLederService.getNarmesteLeder("orgnummer", "1")
            nl shouldBeEqualTo null
        }
        test("Leder med mellomnavn") {
            val navn = narmesteLederService.getName(leder)
            navn shouldBeEqualTo "Fornavn Mellomnavn Etternavn"
        }
        test("Leder uten mellomnavn") {
            val person = leder.copy(mellomnavn = null)
            val navn = narmesteLederService.getName(person)
            navn shouldBeEqualTo "Fornavn Etternavn"
        }
    }
})
