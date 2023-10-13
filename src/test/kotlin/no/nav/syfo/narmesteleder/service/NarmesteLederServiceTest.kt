package no.nav.syfo.narmesteleder.service

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.db.NarmestelederDbModel
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NarmesteLederServiceTest {
    private val narmestelederDb = mockk<NarmestelederDB>()
    private val pdlClient = mockk<PdlClient>()
    private val narmesteLederService = NarmesteLederService(narmestelederDb, pdlClient)

    @BeforeEach
    fun beforeTest() {
        clearAllMocks()
    }

    private val leder = Person("Fornavn", "Mellomnavn", "Etternavn", "aktørid", "lederFnr")

    @Test
    internal fun `NarmestelederService - leder faar riktig navn Should get correct NL`() {
        every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns
            NarmestelederDbModel(
                sykmeldtFnr = "1",
                lederFnr = "lederFnr",
                orgnummer = "orgnummer",
                narmesteLederTelefonnummer = "telefon",
                narmesteLederEpost = "epost",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = true,
            )

        coEvery { pdlClient.getPerson("lederFnr") } returns leder

        runBlocking {
            val nl = narmesteLederService.getNarmesteLeder(orgnummer = "orgnummer", fnr = "1")
            nl shouldBeEqualTo
                NarmesteLeder(
                    "epost",
                    "orgnummer",
                    "telefon",
                    LocalDate.of(2021, 1, 1),
                    true,
                    "Fornavn Mellomnavn Etternavn",
                    "lederFnr",
                )
        }
    }

    @Test
    internal fun `NarmestelederService - leder faar riktig navn Should get null`() {
        every { narmestelederDb.getNarmesteleder("1", "orgnummer") } returns null
        coEvery { pdlClient.getPerson("lederFnr") } returns leder
        runBlocking {
            val nl = narmesteLederService.getNarmesteLeder("orgnummer", "1")
            nl shouldBeEqualTo null
        }
    }

    @Test
    internal fun `NarmestelederService - leder faar riktig navn Leder med mellomnavn`() {
        val navn = narmesteLederService.getName(leder)
        navn shouldBeEqualTo "Fornavn Mellomnavn Etternavn"
    }

    @Test
    internal fun `NarmestelederService - leder faar riktig navn Leder uten mellomnavn`() {
        val person = leder.copy(mellomnavn = null)
        val navn = narmesteLederService.getName(person)
        navn shouldBeEqualTo "Fornavn Etternavn"
    }
}
