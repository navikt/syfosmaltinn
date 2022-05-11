package no.nav.syfo.juridisklogg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class JuridiskLoggClient(private val httpClient: HttpClient, private val url: String, private val apiKey: String) {
    suspend fun logg(log: Logg): JuridiskResponse {
        return httpClient.post(url) {
            headers {
                contentType(ContentType.Application.Json)
                append("Nav-Call-Id", log.meldingsId)
                append("Nav-Consumer-Id", "srvsyfosmaltinn")
                append("x-nav-apikey", apiKey)
            }
            setBody(log)
        }.body()
    }
}
