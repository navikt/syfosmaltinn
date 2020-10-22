package no.nav.syfo.juridisklogg

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class JuridiskLoggClient(private val httpClient: HttpClient, private val url: String) {
    suspend fun logg(log: Logg): JuridiskResponse {
        return httpClient.post(url) {
            headers {
                contentType(ContentType.Application.Json)
                append("Nav-Call-Id", log.meldingsId)
                append("Nav-Consumer-Id", "srvsyfosmaltinn")
            }
            body = log
        }
    }
}
