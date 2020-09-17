package no.nav.syfo.util

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

data class ResponseData(val httpStatusCode: HttpStatusCode, val content: String, val headers: Headers = headersOf("Content-Type", listOf("application/json")))

fun getSerializer(): JacksonSerializer {
    return JacksonSerializer {
        registerKotlinModule()
    }
}

class HttpClientTest {

    var responseData: ResponseData? = null

    val httpClient = HttpClient(MockEngine) {
        install(JsonFeature) {
            getSerializer()
        }
        engine {
            addHandler { request ->
                respond(responseData!!.content, responseData!!.httpStatusCode, responseData!!.headers)
            }
        }
    }
}
