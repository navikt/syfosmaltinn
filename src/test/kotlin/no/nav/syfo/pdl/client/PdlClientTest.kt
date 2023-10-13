package no.nav.syfo.pdl.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import io.mockk.coEvery
import io.mockk.mockk
import java.io.File
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import no.nav.syfo.azuread.AccessTokenClient
import org.junit.jupiter.api.Test

internal class PdlClientTest {
    private val accessTokenClient = mockk<AccessTokenClient>()

    private val httpClient =
        HttpClient(MockEngine) {
            install(ContentNegotiation) {
                jackson {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            engine {
                addHandler { _ ->
                    respond(
                        getTestData(),
                        HttpStatusCode.OK,
                        headersOf("Content-Type", "application/json"),
                    )
                }
            }
        }

    private val graphQlQuery =
        File("src/main/resources/graphql/getPerson.graphql").readText().replace(Regex("[\n\t]"), "")
    private val pdlClient =
        PdlClient(httpClient, "graphqlend", graphQlQuery, accessTokenClient, "scope")

    @Test
    internal fun `getPerson OK Kaster exception hvis person ikke finnes i PDL`() {
        coEvery { accessTokenClient.getAccessToken(any()) } returns "token"
        runBlocking { assertFailsWith<Exception> { pdlClient.getPerson("12345678901") } }
    }
}
