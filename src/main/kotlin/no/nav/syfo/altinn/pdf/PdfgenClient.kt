package no.nav.syfo.altinn.pdf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.log
import no.nav.syfo.securelog

class PdfgenClient(
    private val url: String,
    private val httpClient: HttpClient,
) {
    suspend fun createPdf(payload: PdfPayload, sykmeldingsId: String): ByteArray {
        val objectMapper: ObjectMapper =
            jacksonObjectMapper().apply {
                registerModule(JavaTimeModule())
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            }

        securelog.info("PdfPayload: " + objectMapper.writeValueAsString(payload))
        val httpResponse =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
        if (httpResponse.status == HttpStatusCode.OK) {
            return httpResponse.call.response.body()
        } else {
            log.error(
                "Mottok feilkode fra smarbeidsgiver-pdfgen: {}, for sykmeldingid $sykmeldingsId",
                httpResponse.status
            )
            throw RuntimeException(
                "Mottok feilkode fra smarbeidsgiver-pdfgen: ${httpResponse.status}, for sykmeldingid $sykmeldingsId"
            )
        }
    }
}
