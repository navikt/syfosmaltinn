package no.nav.syfo.narmesteleder.service

import io.mockk.mockk
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
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BeOmNyNLServiceTest : Spek({
    val nlRequestProducer = mockk<NLRequestProducer>(relaxed = true)
    val nlResponseProducer = mockk<NLResponseProducer>(relaxed = true)
    val beOmNyNLService = BeOmNyNLService(nlRequestProducer, nlResponseProducer)

    describe("BeOmNyNLService") {
        it("Skal be om ny NL hvis det er svart ja på spørsmål om NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(listOf(
                SporsmalOgSvarDTO("Be om ny nærmeste leder?", ShortNameDTO.NY_NARMESTE_LEDER, SvartypeDTO.JA_NEI, "JA")
            ))

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, getNarmesteleder(forskutterer = false)) shouldEqual true
        }
        it("Skal be om ny NL hvis det ikke er registrert noen NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(emptyList())

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, null) shouldEqual true
        }
        it("Skal be om ny NL hvis det ikke er svart på om NL forskutterer lønn") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                emptyList())

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, getNarmesteleder(forskutterer = null)) shouldEqual true
        }
        it("Skal ikke be om ny NL hvis NL er registrert og har svart på forskuttering og bruker ikke har bedt om ny NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(
                emptyList())

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, getNarmesteleder(forskutterer = false)) shouldEqual false
        }
        it("Skal ikke be om ny NL hvis det er svart nei på spørsmål om NL") {
            val sykmeldingStatusKafkaEvent = getSykmeldingStatusKafkaEvent(listOf(
                SporsmalOgSvarDTO("Be om ny nærmeste leder?", ShortNameDTO.NY_NARMESTE_LEDER, SvartypeDTO.JA_NEI, "NEI")
            ))

            beOmNyNLService.skalBeOmNyNL(sykmeldingStatusKafkaEvent, null) shouldEqual false
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
        aktorId = "aktorId",
        epost = "epost@nav.no",
        orgnummer = "orgnummer",
        telefonnummer = "99999999",
        aktivFom = LocalDate.now().minusYears(2),
        arbeidsgiverForskutterer = forskutterer,
        skrivetilgang = true,
        tilganger = emptyList(),
        navn = "Leder Ledersen",
        fnr = "fnr"
    )
}
