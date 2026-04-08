package no.nav.syfo.altinn.pdf

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.arbeidsgiver.AktivitetIkkeMuligAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TypstClientTest {

    private val pdfBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)

    private val payload =
        PdfPayload(
            ansatt = Ansatt(fnr = "12345678910", navn = "Navn Navnesen"),
            narmesteleder =
                NarmesteLeder(
                    epost = "leder@nav.no",
                    orgnummer = "123456789",
                    telefonnummer = "90909090",
                    aktivFom = LocalDate.of(2020, 1, 1),
                    arbeidsgiverForskutterer = null,
                    navn = "Leder Ledersen",
                    fnr = "01987654321",
                ),
            arbeidsgiverSykmelding =
                ArbeidsgiverSykmeldingPdf(
                    id = "sykmelding-id",
                    syketilfelleStartDato = LocalDate.of(2020, 1, 1),
                    behandletTidspunkt = OffsetDateTime.parse("2020-01-01T12:00:00Z"),
                    arbeidsgiverNavn = "Min Bedrift",
                    sykmeldingsperioder =
                        listOf(
                            SykmeldingsperiodePdf(
                                fom = LocalDate.of(2020, 1, 1),
                                tom = LocalDate.of(2020, 1, 10),
                                varighet = 10,
                                gradert = null,
                                behandlingsdager = null,
                                innspillTilArbeidsgiver = null,
                                type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                                aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                                reisetilskudd = false,
                            ),
                        ),
                    prognose = null,
                    tiltakArbeidsplassen = null,
                    meldingTilArbeidsgiver = null,
                    behandler = BehandlerPdf(navn = "Lege Legesen", tlf = "12345678"),
                    egenmeldingsdager = null,
                    kontaktMedPasient = KontaktMedPasientAGDTO(kontaktDato = LocalDate.of(2020, 1, 1)),
                ),
        )

    private fun buildMockClient(exitCode: Int, stdout: ByteArray, stderr: String): TypstClient {
        val mockProcess = mockk<Process>()
        every { mockProcess.inputStream } returns ByteArrayInputStream(stdout)
        every { mockProcess.errorStream } returns ByteArrayInputStream(stderr.toByteArray())
        every { mockProcess.waitFor() } returns exitCode

        val mockProcessBuilder = mockk<ProcessBuilder>()
        every { mockProcessBuilder.redirectError(any()) } returns mockProcessBuilder
        every { mockProcessBuilder.start() } returns mockProcess

        return TypstClient(processBuilderFactory = { _ -> mockProcessBuilder })
    }

    @Test
    fun `createPdf returns bytes on successful compilation`() {
        val client = buildMockClient(exitCode = 0, stdout = pdfBytes, stderr = "")

        val result = client.createPdf(payload)

        result shouldBeEqualTo pdfBytes
    }

    @Test
    fun `createPdf throws RuntimeException when exit code is non-zero`() {
        val stderrMessage = "error: unknown variable: foo"
        val client = buildMockClient(exitCode = 1, stdout = ByteArray(0), stderr = stderrMessage)

        val exception = assertThrows<RuntimeException> { client.createPdf(payload) }

        exception.message shouldContain stderrMessage
    }

    @Test
    fun `createPdf includes serialized payload JSON in the command`() {
        val commandSlot = slot<List<String>>()
        val mockProcess = mockk<Process>()
        every { mockProcess.inputStream } returns ByteArrayInputStream(pdfBytes)
        every { mockProcess.errorStream } returns ByteArrayInputStream(ByteArray(0))
        every { mockProcess.waitFor() } returns 0

        val mockProcessBuilder = mockk<ProcessBuilder>()
        every { mockProcessBuilder.redirectError(any()) } returns mockProcessBuilder
        every { mockProcessBuilder.start() } returns mockProcess

        val client =
            TypstClient(
                processBuilderFactory = { commands ->
                    commandSlot.captured = commands
                    mockProcessBuilder
                }
            )

        client.createPdf(payload)

        val inputArg = commandSlot.captured.first { it.startsWith("--input=data=") }
        inputArg shouldContain "\"fnr\":\"12345678910\""
        inputArg shouldContain "\"navn\":\"Navn Navnesen\""
        inputArg shouldContain "\"id\":\"sykmelding-id\""
    }

    @Test
    fun `createPdf works with null narmesteleder`() {
        val payloadWithoutNl = payload.copy(narmesteleder = null)
        val client = buildMockClient(exitCode = 0, stdout = pdfBytes, stderr = "")

        val result = client.createPdf(payloadWithoutNl)

        result shouldBeEqualTo pdfBytes
    }
}
