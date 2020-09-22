package no.nav.syfo.narmesteleder

import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.log

class NarmestelederClient(private val httpClient: HttpClient, private val accessTokenClient: AccessTokenClient, private val baseUrl: String) {

    suspend fun getNarmesteleder(orgnummer: String, aktorId: String): NarmestelederResponse {
        val token = accessTokenClient.getAccessToken()
        val statement = httpClient.get<HttpStatement>("$baseUrl$NARMESTE_LEDER_URL/$aktorId?orgnummer=$orgnummer") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }.execute()
        val status = statement.status
        log.info("Got status $status from NarmesteLeder")
        return statement.receive()

    }

    companion object {
        private const val NARMESTE_LEDER_URL = "/syfonarmesteleder/sykmeldt/"
    }
}
