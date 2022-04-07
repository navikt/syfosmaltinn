package no.nav.syfo.narmesteleder.service

import io.kotest.core.spec.style.FunSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import java.time.LocalDate
import java.time.OffsetDateTime

class BeOmNyNLServiceTest : FunSpec({
    val nlRequestProducer = mockk<NLRequestProducer>(relaxed = true)
    val nlResponseProducer = mockk<NLResponseProducer>(relaxed = true)
    val database = mockk<DatabaseInterface>()
    mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
    val beOmNyNLService = BeOmNyNLService(nlRequestProducer, nlResponseProducer, database)

    afterTest {
        mockkStatic("no.nav.syfo.sykmelding.db.DatabaseQueriesKt")
        clearMocks(database)
    }

    beforeTest {
        every { database.hasCheckedNl(any()) } returns false
    }

    context("BeOmNyNLService") {
        test("Skal ikke be om ny NL om det er gjort") {
            every { database.hasCheckedNl(any()) } returns true
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA"
                    )
                )
            )
            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, getNarmesteleder(true)) shouldBeEqualTo false
        }

        test("Skal ikke be om ny NL om det ikke er gjort") {
            every { database.hasCheckedNl(any()) } returns false
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA"
                    )
                )
            )
            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, getNarmesteleder(true)) shouldBeEqualTo true
        }

        test("Skal be om ny NL hvis det er svart ja på spørsmål om NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "JA"
                    )
                )
            )

            beOmNyNLService.skalBeOmNyNL(
                sykmeldingStatusKafkaEvent,
                getNarmesteleder(forskutterer = false)
            ) shouldBeEqualTo true
        }
        test("Skal be om ny NL hvis det ikke er registrert noen NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(emptyList())

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, null) shouldBeEqualTo true
        }
        test("Skal be om ny NL hvis det ikke er svart på om NL forskutterer lønn") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                emptyList()
            )

            beOmNyNLService.skalBeOmNyNL(
                sykmeldingStatusKafkaEvent,
                getNarmesteleder(forskutterer = null)
            ) shouldBeEqualTo true
        }
        test("Skal ikke be om ny NL hvis NL er registrert og har svart på forskuttering og bruker ikke har bedt om ny NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                emptyList()
            )

            beOmNyNLService.skalBeOmNyNL(
                sykmeldingStatusKafkaEvent,
                getNarmesteleder(forskutterer = false)
            ) shouldBeEqualTo false
        }
        test("Skal ikke be om ny NL hvis det er svart nei på spørsmål om NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                listOf(
                    SporsmalOgSvarDTO(
                        "Be om ny nærmeste leder?",
                        ShortNameDTO.NY_NARMESTE_LEDER,
                        SvartypeDTO.JA_NEI,
                        "NEI"
                    )
                )
            )

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, null) shouldBeEqualTo false
        }
    }
})

fun getSykmeldingStatusKafkaEvent(sporsmalOgSvarListe: List<SporsmalOgSvarDTO>): SykmeldingStatusKafkaEventDTO {
    return SykmeldingStatusKafkaEventDTO(
        sykmeldingId = "sykmeldingId",
        arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
        sporsmals = sporsmalOgSvarListe,
        statusEvent = "SENDT",
        timestamp = OffsetDateTime.now()
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
        fnr = "fnr"
    )
}
