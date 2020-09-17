package no.nav.syfo.narmesteleder

import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.util.HttpClientTest
import no.nav.syfo.util.ResponseData
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

class NarmestelederClientTest : Spek({
    val httpClientTest = HttpClientTest()
    val accessTokenClient = mockk<AccessTokenClient>()
    coEvery { accessTokenClient.getAccessToken() } returns "token"
    val narmesteLederClient = NarmestelederClient(httpClientTest.httpClient, accessTokenClient)

    describe("Get response from NarmesteLeder") {
        it("get null response") {
            runBlocking {
                httpClientTest.responseData = ResponseData(HttpStatusCode.OK, "{ \"narmesteLederRelasjon\": null}")
                val response = narmesteLederClient.getNarmesteleder("org", "aktor")
                response shouldNotBe null
                response.narmesteLederRelasjon shouldBe null
            }
        }

        it("get unauthorized response") {
            runBlocking {
                httpClientTest.responseData = ResponseData(HttpStatusCode.Unauthorized, "Unauthorized", headersOf("Content-Type", listOf("Text")))
                val exception = assertFailsWith<ClientRequestException> { narmesteLederClient.getNarmesteleder("org", "aktor") }
                exception.response.status shouldEqual HttpStatusCode.Unauthorized
            }
        }

        it("get not found response") {
            runBlocking {
                httpClientTest.responseData = ResponseData(HttpStatusCode.NotFound, "Not found", headersOf("Content-Type", listOf("Text")))
                val exception = assertFailsWith<ClientRequestException> { narmesteLederClient.getNarmesteleder("org", "aktor") }
                exception.response.status shouldEqual HttpStatusCode.NotFound
            }
        }
    }
})
