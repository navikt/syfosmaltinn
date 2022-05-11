package no.nav.syfo.pdl.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import no.nav.syfo.log
import no.nav.syfo.pdl.client.model.GetPersonRequest
import no.nav.syfo.pdl.client.model.GetPersonResponse
import no.nav.syfo.pdl.client.model.GetPersonVeriables
import no.nav.syfo.pdl.client.model.Person
import no.nav.syfo.pdl.client.model.toPerson

class PdlClient(
    private val httpClient: HttpClient,
    private val basePath: String,
    private val apiKey: String,
    private val graphQlQuery: String
) {

    private val navConsumerToken = "Nav-Consumer-Token"
    private val temaHeader = "TEMA"
    private val tema = "SYM"

    suspend fun getPerson(ident: String, stsToken: String): Person {
        val getPersonRequest = GetPersonRequest(query = graphQlQuery, variables = GetPersonVeriables(ident = ident))
        val pdlResponse = httpClient.post(basePath) {
            setBody(getPersonRequest)
            header("x-nav-apikey", apiKey)
            header(HttpHeaders.Authorization, "Bearer $stsToken")
            header(temaHeader, tema)
            header(HttpHeaders.ContentType, "application/json")
            header(navConsumerToken, "Bearer $stsToken")
        }.body<GetPersonResponse>()
        try {
            return pdlResponse.toPerson()
        } catch (e: Exception) {
            log.error("Error when getting pdlResponse")
            throw e
        }
    }
}
