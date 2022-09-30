package no.nav.syfo.altinn.pdf

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.log

class PdfgenClient(
    private val url: String,
    private val httpClient: HttpClient
) {
    suspend fun createPdf(payload: PdfPayload): ByteArray {
        val httpResponse = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (httpResponse.status == HttpStatusCode.OK) {
            return httpResponse.call.response.body()
        } else {
            log.error("Mottok feilkode fra smarbeidsgiver-pdfgen: {}", httpResponse.status)
            throw RuntimeException("Mottok feilkode fra smarbeidsgiver-pdfgen: ${httpResponse.status}")
        }
    }
}
