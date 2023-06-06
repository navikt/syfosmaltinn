package no.nav.syfo.altinn.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.ArbeidsgiverSykmelding
import no.nav.syfo.model.sykmelding.arbeidsgiver.BehandlerAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.PrognoseAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.SykmeldingsperiodeAGDTO
import no.nav.syfo.model.sykmelding.model.AdresseDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.model.sykmeldingstatus.ArbeidsgiverStatusDTO
import no.nav.syfo.model.sykmeldingstatus.KafkaMetadataDTO
import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBe
import org.jsoup.Jsoup
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

class AltinnSykmeldingMapperTest : FunSpec({
    context("Test sykmelding mapper") {
        test("Should create InsertCorrespondenceV2") {
            val sykmeldingId = "uuid"
            val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            val sendtSykmeldingKafkaMessage = getAivenMessage(sykmeldingId, timestamp)
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr",
            )
            val narmesteLeder = NarmesteLeder(
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                "NL Navn",
                "fnrLeder",
            )
            val sykmeldingAltinn = SykmeldingAltinn(
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, pasient),
                narmesteLeder,
                emptyList(),
                "pdf".toByteArray(),
            )

            val insertCorrespondanceV2 = AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
                sykmeldingAltinn,
                sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn).filterNotNull().joinToString(" "),
                sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer,
            )
            insertCorrespondanceV2 shouldNotBe null
        }
        test("Should create empty string when behandler telefonnummer is null") {
            val sykmeldingId = "uuid"
            val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            val sendtSykmeldingKafkaMessage = getAivenMessage(sykmeldingId, timestamp)
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr",
            )

            val xmlSykmeldingArbeidsgiver =
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, pasient)

            xmlSykmeldingArbeidsgiver.sykmelding.behandler.telefonnummer shouldBeEqualTo ""
        }
    }

    context("HTML generation") {
        test("should include egenmeldingsdager") {
            val sykmeldingId = "uuid"
            val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            val sendtSykmeldingKafkaMessage = getAivenMessage(sykmeldingId, timestamp)
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr",
            )
            val narmesteLeder = NarmesteLeder(
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                "NL Navn",
                "fnrLeder",
            )
            val sykmeldingAltinn = SykmeldingAltinn(
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, pasient),
                narmesteLeder,
                listOf(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-06")),
                "pdf".toByteArray(),
            )

            val dom = Jsoup.parse(sykmeldingAltinn.sykmeldingHTML)
            val datesFromDom = dom
                .body()
                .select("div.sykmelding-nokkelopplysning:contains(Egenmeldingsdager)")
                .select("li")
                .map { it.text() }

            datesFromDom shouldBeEqual listOf("01.01.2021", "06.01.2021")
        }

        test("should not show section when no egenmeldingsdager") {
            val sykmeldingId = "uuid"
            val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
            val sendtSykmeldingKafkaMessage = getAivenMessage(sykmeldingId, timestamp)
            System.setProperty("environment.name", "local")
            val pasient = Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr",
            )
            val narmesteLeder = NarmesteLeder(
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                "NL Navn",
                "fnrLeder",
            )
            val sykmeldingAltinn = SykmeldingAltinn(
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(sendtSykmeldingKafkaMessage, pasient),
                narmesteLeder,
                null,
                "pdf".toByteArray(),
            )

            val dom = Jsoup.parse(sykmeldingAltinn.sykmeldingHTML)
            val elements = dom
                .body()
                .select("div.sykmelding-nokkelopplysning:contains(Egenmeldingsdager)")

            elements shouldHaveSize 0
        }
    }
})

private fun getAivenMessage(sykmeldingId: String, timestamp: OffsetDateTime) = SendSykmeldingAivenKafkaMessage(
    sykmelding = ArbeidsgiverSykmelding(
        id = sykmeldingId,
        arbeidsgiver = ArbeidsgiverAGDTO("ArbeidsgiverNavn", "yrke"),
        behandler = BehandlerAGDTO(
            "BehandlerFornavn",
            "BehandlerMellomnavn",
            "BehandlerEtternavn",
            "aktorid",
            AdresseDTO(
                null,
                null,
                null,
                null,
                null,
            ),
            null,
        ),
        behandletTidspunkt = timestamp,
        egenmeldt = false,
        harRedusertArbeidsgiverperiode = false,
        kontaktMedPasient = KontaktMedPasientAGDTO(LocalDate.of(2016, 12, 7)),
        meldingTilArbeidsgiver = "MeldingTilArbeidsgiver",
        mottattTidspunkt = timestamp,
        papirsykmelding = false,
        prognose = PrognoseAGDTO(
            arbeidsforEtterPeriode = false,
            hensynArbeidsplassen = "BeskrivHensynArbeidsplassen",
        ),
        syketilfelleStartDato = LocalDate.of(2016, 12, 7),
        sykmeldingsperioder = listOf(
            SykmeldingsperiodeAGDTO(
                LocalDate.of(2016, 12, 7),
                LocalDate.of(2016, 12, 7),
                null,
                null,
                "AvventendeSykmelding",
                PeriodetypeDTO.AVVENTENDE,
                null,
                false,
            ),
        ),
        tiltakArbeidsplassen = "TiltakArbeidsplassen",
        merknader = emptyList(),
        utenlandskSykmelding = null,
    ),
    event = SykmeldingStatusKafkaEventDTO(
        sykmeldingId = sykmeldingId,
        arbeidsgiver = ArbeidsgiverStatusDTO("orgnummer", "1234", "orgnavn"),
        sporsmals = emptyList(),
        statusEvent = "SENDT",
        timestamp = OffsetDateTime.now(),
    ),
    kafkaMetadata = KafkaMetadataDTO(sykmeldingId, OffsetDateTime.now(), "fnr", "syfoservice"),
)

/**
 * Can be used to preview for example the portable HTML in a browser
 *
 * Example: `sykmeldingAltinn.sykmeldingPortableHTML.openInFirefox()`
 */
private fun String.openInFirefox() {
    val encodedHtml = Base64.getEncoder().encodeToString(this.toByteArray())
    val uri = URI.create("data:text/html;base64,$encodedHtml")
    val processBuilder = ProcessBuilder("firefox", uri.toString())
    processBuilder.start()
}
