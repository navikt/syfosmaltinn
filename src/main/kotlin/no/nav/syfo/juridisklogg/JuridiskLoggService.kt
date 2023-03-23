package no.nav.syfo.juridisklogg

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.application.metrics.JURIDISK_LOGG_COUNTER
import no.nav.syfo.log
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPOutputStream

class JuridiskLoggService(
    private val bucketName: String,
    private val storage: Storage,
) {

    fun sendJuridiskLogg(
        sykmeldingAltinn: SykmeldingAltinn,
        sykmeldingId: String,
    ) {
        val mappe = "$sykmeldingId/${mappeTidspunkt()}/"
        fun lagreFil(filnavn: String, contentType: String, content: ByteArray) {
            val blobInfo = BlobInfo.newBuilder(
                BlobId.of(bucketName, mappe + filnavn),
            ).setContentType(contentType).build()
            storage.create(blobInfo, content)
        }

        lagreFil(
            filnavn = "sykmelding.pdf",
            contentType = "application/pdf",
            content = sykmeldingAltinn.sykmeldingPdf,
        )
        lagreFil(
            filnavn = "sykmelding.xml",
            contentType = "application/xml",
            content = sykmeldingAltinn.sykmeldingXml.toByteArray(),
        )

        log.info("Lastet opp dokumenter til mappe $mappe i juridisk logg-bucket")
        JURIDISK_LOGG_COUNTER.inc()
    }

    private fun String.gzip(): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(this) }
        return bos.toByteArray()
    }

    private fun mappeTidspunkt() =
        Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace("-", "").replace(":", "")
}
