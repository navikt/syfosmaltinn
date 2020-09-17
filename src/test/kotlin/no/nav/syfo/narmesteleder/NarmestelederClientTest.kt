package no.nav.syfo.narmesteleder

import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.azuread.AccessTokenClient
import no.nav.syfo.util.HttpClientTest
import no.nav.syfo.util.ResponseData
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldNotBe
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

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
    }
})
