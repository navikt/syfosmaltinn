package no.nav.syfo.narmesteleder.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.ShortNameDTO
import no.nav.syfo.model.sykmeldingstatus.SporsmalOgSvarDTO
import no.nav.syfo.model.sykmeldingstatus.SvartypeDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.kafka.NLRequestProducer
import no.nav.syfo.narmesteleder.kafka.NLResponseProducer
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.sykmelding.db.DatabaseInterface
import no.nav.syfo.sykmelding.db.hasCheckedNl
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BeOmNyNLServiceTest {
    private val nlRequestProducer = mockk<NLRequestProducer>(relaxed = true)
    private val nlResponseProducer = mockk<NLResponseProducer>(relaxed = true)
    private val database = mockk<DatabaseInterface>()
    private val beOmNyNLService = BeOmNyNLService(nlRequestProducer, nlResponseProducer, database)

    @AfterEach
    fun afterTest() {
        mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
        clearMocks(database)
    }

    @BeforeAll
    fun beforeAll() {
        mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
    }

    @BeforeEach
    fun beforeTest() {
        every { database.hasCheckedNl(any()) } returns false
    }

    @Test
    internal fun `Skal ikke be om ny NL om det er gjort`() {
        every { database.hasCheckedNl(any()) } returns true
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA",
                    ),
                ),
            )
        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            getNarmesteleder(true),
        ) shouldBeEqualTo false
    }

    @Test
    internal fun `Skal ikke be om ny NL om det ikke er gjort`() {
        every { database.hasCheckedNl(any()) } returns false
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA",
                    ),
                ),
            )
        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            getNarmesteleder(true),
        ) shouldBeEqualTo true
    }

    @Test
    internal fun `Skal be om ny NL hvis det er svart ja på spørsmål om NL`() {
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA",
                    ),
                ),
            )

        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            getNarmesteleder(forskutterer = false),
        ) shouldBeEqualTo true
    }

    @Test
    internal fun `Skal be om ny NL hvis det ikke er registrert noen NL`() {
        val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(emptyList())

        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            null,
        ) shouldBeEqualTo true
    }

    @Test
    internal fun `Skal be om ny NL hvis det ikke er svart på om NL forskutterer lønn`() {
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                emptyList(),
            )

        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            getNarmesteleder(forskutterer = null),
        ) shouldBeEqualTo true
    }

    @Test
    internal fun `Skal ikke be om ny NL hvis NL er registrert og har svart på forskuttering og bruker ikke har bedt om ny NL`() {
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                emptyList(),
            )

        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            getNarmesteleder(forskutterer = false),
        ) shouldBeEqualTo false
    }

    @Test
    internal fun `Skal ikke be om ny NL hvis det er svart nei på spørsmål om NL`() {
        val sykmeldingStatusKafkaEvent =
            getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "NEI",
                    ),
                ),
            )

        beOmNyNLService.skalBeOmNyNL(
            sykmeldingStatusKafkaEvent,
            null,
        ) shouldBeEqualTo false
    }
}

fun getSykmeldingStatusKafkaEvent(
    sporsmalOgSvarListe: List<SporsmalOgSvarDTO>
): SykmeldingStatusKafkaEventDTO {
    return SykmeldingStatusKafkaEventDTO(
        sykmeldingId = "sykmeldingId",
        arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
        sporsmals = sporsmalOgSvarListe,
        statusEvent = "SENDT",
        timestamp = OffsetDateTime.now(),
    )
}

fun getNarmesteleder(forskutterer: Boolean?): NarmesteLeder {
    return NarmesteLeder(
        epost = "epost@nav.no",
        orgnummer = "orgnummer",
        telefonnummer = "99999999",
        aktivFom = LocalDate.now().minusYears(2),
        arbeidsgiverForskutterer = forskutterer,
        navn = "Leder Ledersen",
        fnr = "fnr",
    )
}
