package no.nav.syfo.sykmelding.altinn.model

import java.time.LocalDate
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.altinn.model.SykmeldingArbeidsgiverMapper
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.model.Person
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.Test

internal class AltinnSykmeldingMapperTest {
    @Test
    internal fun `Test sykmelding mapper Shuold create InsertCorrespondenceV2`() {
        val sykmeldingId = "uuid"
        val sendtSykmeldingKafkaMessage = getSykmeldingKafkaMessage(sykmeldingId)
        System.setProperty("environment.name", "local")
        val pasient =
            Person(
                "PasientFornavn",
                "PasientMellomnavn",
                "PasientEtternavn",
                "aktorid",
                "fnr",
            )
        val narmesteLeder =
            NarmesteLeder(
                "nl-epost",
                "orgnummer",
                "Telefonnummer",
                LocalDate.now(),
                null,
                "NL Navn",
                "fnrLeder",
            )
        val egenmeldingsdager =
            listOf<LocalDate>(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-06"))

        val sykmeldingAltinn =
            SykmeldingAltinn(
                SykmeldingArbeidsgiverMapper.toAltinnXMLSykmelding(
                    sendtSykmeldingKafkaMessage,
                    pasient,
                    egenmeldingsdager,
                ),
                narmesteLeder,
                emptyList(),
                "pdf".toByteArray(),
                "123123",
            )

        val insertCorrespondanceV2 =
            AltinnSykmeldingMapper.sykmeldingTilCorrespondence(
                sykmeldingAltinn,
                sequenceOf(pasient.fornavn, pasient.mellomnavn, pasient.etternavn)
                    .filterNotNull()
                    .joinToString(" "),
                sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer,
            )
        insertCorrespondanceV2 shouldNotBe null
    }
}
