package no.nav.syfo.altinn.pdf

import java.nio.file.Files
import java.time.LocalDate
import java.time.OffsetDateTime
import no.nav.syfo.model.sykmelding.arbeidsgiver.AktivitetIkkeMuligAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.KontaktMedPasientAGDTO
import no.nav.syfo.model.sykmelding.arbeidsgiver.PrognoseAGDTO
import no.nav.syfo.model.sykmelding.model.PeriodetypeDTO
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TypstClientTest {

    private val typstContainer =
        GenericContainer(DockerImageName.parse("ghcr.io/typst/typst:latest"))
            .withCommand("--version")

    private lateinit var typstBinaryPath: String
    private lateinit var templatePath: String
    private lateinit var fontPath: String
    private lateinit var tempDir: java.io.File

    @BeforeAll
    fun setup() {
        typstContainer.start()

        tempDir = Files.createTempDirectory("typst-test").toFile()

        val binaryFile = tempDir.resolve("typst")
        typstContainer.copyFileFromContainer("/bin/typst", binaryFile.absolutePath)
        binaryFile.setExecutable(true)
        typstBinaryPath = binaryFile.absolutePath

        val typstPdfDir = findTypstPdfDir()
        fontPath = "$typstPdfDir/fonts"
        templatePath = "$typstPdfDir/smarbeidsgiver.typ"
    }

    private fun findTypstPdfDir(): String {
        var dir: java.io.File? = java.io.File(System.getProperty("user.dir"))
        var depth = 0
        while (dir != null && depth < 6) {
            val candidate = dir.resolve("typst-pdf")
            if (candidate.isDirectory && candidate.resolve("smarbeidsgiver.typ").exists()) {
                return candidate.absolutePath
            }
            dir = dir.parentFile
            depth++
        }
        error("Could not find typst-pdf directory containing smarbeidsgiver.typ")
    }

    @AfterAll
    fun teardown() {
        typstContainer.stop()
        tempDir.deleteRecursively()
    }

    @Test
    fun `createPdf generates valid PDF bytes`() {
        val typstClient =
            TypstClient(
                typstBinaryPath = typstBinaryPath,
                templatePath = templatePath,
                fontPath = fontPath,
            )

        val pdfBytes = typstClient.createPdf(buildPdfPayload())

        assertTrue(pdfBytes.isNotEmpty(), "PDF output should not be empty")
        assertTrue(
            pdfBytes.size >= 4 && String(pdfBytes, 0, 4) == "%PDF",
            "Output should start with %PDF header",
        )
    }

    private fun buildPdfPayload(): PdfPayload =
        PdfPayload(
            ansatt =
                Ansatt(
                    fnr = "12345678910",
                    navn = "Test Testersen",
                ),
            narmesteleder =
                NarmesteLeder(
                    epost = "leder@nav.no",
                    orgnummer = "999888777",
                    telefonnummer = "90909090",
                    aktivFom = LocalDate.of(2022, 1, 1),
                    arbeidsgiverForskutterer = false,
                    navn = "Leder Ledersen",
                    fnr = "01987654321",
                ),
            arbeidsgiverSykmelding =
                ArbeidsgiverSykmeldingPdf(
                    id = "test-sykmelding-id",
                    syketilfelleStartDato = LocalDate.of(2023, 1, 1),
                    behandletTidspunkt = OffsetDateTime.parse("2023-01-05T10:00:00Z"),
                    arbeidsgiverNavn = "Test Arbeidsgiver AS",
                    sykmeldingsperioder =
                        listOf(
                            SykmeldingsperiodePdf(
                                fom = LocalDate.of(2023, 1, 1),
                                tom = LocalDate.of(2023, 1, 14),
                                varighet = 14,
                                gradert = null,
                                behandlingsdager = null,
                                innspillTilArbeidsgiver = null,
                                type = PeriodetypeDTO.AKTIVITET_IKKE_MULIG,
                                aktivitetIkkeMulig = AktivitetIkkeMuligAGDTO(null),
                                reisetilskudd = false,
                            ),
                        ),
                    prognose =
                        PrognoseAGDTO(
                            arbeidsforEtterPeriode = true,
                            hensynArbeidsplassen = "Trenger tilrettelagt arbeid",
                        ),
                    tiltakArbeidsplassen = "Tilrettelegging av arbeidsplass",
                    meldingTilArbeidsgiver = "Melding til arbeidsgiver",
                    behandler =
                        BehandlerPdf(
                            navn = "Lege Legesen",
                            tlf = "12345678",
                        ),
                    egenmeldingsdager = listOf(LocalDate.of(2022, 12, 30)),
                    kontaktMedPasient =
                        KontaktMedPasientAGDTO(kontaktDato = LocalDate.of(2023, 1, 1)),
                ),
        )
}
