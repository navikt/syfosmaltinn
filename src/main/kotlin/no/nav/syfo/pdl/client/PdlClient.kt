package no.nav.syfo.pdl.client

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import java.lang.RuntimeException
import no.nav.syfo.log
import no.nav.syfo.pdl.client.model.GetPersonRequest
import no.nav.syfo.pdl.client.model.GetPersonResponse
import no.nav.syfo.pdl.client.model.GetPersonVeriables
import no.nav.syfo.pdl.client.model.Navn

class PdlClient(
    private val httpClient: HttpClient,
    private val basePath: String,
    private val graphQlQuery: String
) {

    private val navConsumerToken = "Nav-Consumer-Token"
    private val temaHeader = "TEMA"
    private val tema = "SYM"

    suspend fun getNavn(fnr: String, stsToken: String, sykmeldingId: String): Navn {
        val getPersonRequest = GetPersonRequest(query = graphQlQuery, variables = GetPersonVeriables(ident = fnr))
        val pdlResponse = httpClient.post<GetPersonResponse>(basePath) {
            body = getPersonRequest
            header(HttpHeaders.Authorization, "Bearer $stsToken")
            header(temaHeader, tema)
            header(HttpHeaders.ContentType, "application/json")
            header(navConsumerToken, "Bearer $stsToken")
        }

        if (pdlResponse.data.hentPerson == null) {
            log.error("Fant ikke person i PDL {} for sykmelidng: {}", sykmeldingId)
            throw RuntimeException("Fant ikke person i PDL")
        }
        if (pdlResponse.data.hentPerson.navn.isNullOrEmpty()) {
            log.error("Fant ikke navn på person i PDL {} for sykmelding: {}", sykmeldingId)
            throw RuntimeException("Fant ikke navn på person i PDL")
        }
        return pdlResponse.data.hentPerson.navn.first()
    }
}
