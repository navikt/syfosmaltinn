package no.nav.syfo.narmesteleder

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import no.nav.syfo.azuread.AccessTokenClient

class NarmestelederClient(private val httpClient: HttpClient, private val accessTokenClient: AccessTokenClient, private val baseUrl: String) {

    suspend fun getNarmesteleder(orgnummer: String, aktorId: String): NarmestelederResponse {
        val token = accessTokenClient.getAccessToken()
        return httpClient.get("$baseUrl$NARMESTE_LEDER_URL/$aktorId?orgnummer=$orgnummer") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }
    }

    companion object {
        private const val NARMESTE_LEDER_URL = "/syfonarmesteleder/sykmeldt/"
    }
}
