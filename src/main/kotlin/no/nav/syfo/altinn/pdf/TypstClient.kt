package no.nav.syfo.altinn.pdf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.syfo.logger

class TypstClient(
    private val typstBinaryPath: String = "/app/typst-pdf/typst",
    private val templatePath: String = "/app/typst-pdf/smarbeidsgiver.typ",
    private val fontPath: String = "/app/typst-pdf/fonts",
) {
    private val objectMapper =
        jacksonObjectMapper().apply {
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        }

    fun createPdf(payload: PdfPayload): ByteArray {
        val jsonData = objectMapper.writeValueAsString(payload)

        val process =
            ProcessBuilder(
                    typstBinaryPath,
                    "compile",
                    "--pdf-standard=a-2a",
                    "--font-path=$fontPath",
                    "--input=data=$jsonData",
                    templatePath,
                    "-",
                )
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        var stderr = ""
        val stderrThread = Thread { stderr = process.errorStream.bufferedReader().readText() }
        stderrThread.start()
        val pdfBytes = process.inputStream.readBytes()
        stderrThread.join()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("Typst compilation failed with exit code $exitCode: $stderr")
            throw RuntimeException("Typst compilation failed: $stderr")
        }

        return pdfBytes
    }
}
