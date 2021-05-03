package no.nav.syfo.narmesteleder.service

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.narmesteleder.client.NarmestelederClient
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person

@KtorExperimentalAPI
class NarmesteLederService(
    private val narmestelederClient: NarmestelederClient,
    private val pdlClient: PdlClient,
    private val stsOidcClient: StsOidcClient
) {
    suspend fun getNarmesteLeder(orgnummer: String, fnr: String): NarmesteLeder? {
        return narmestelederClient.getNarmesteleder(
            orgnummer,
            fnr
        ).narmesteLederRelasjon?.let { narmesteLederRelasjon ->

            val lederPerson = pdlClient.getPerson(narmesteLederRelasjon.narmesteLederFnr, stsOidcClient.oidcToken().access_token)

            NarmesteLeder(
                epost = narmesteLederRelasjon.narmesteLederEpost,
                orgnummer = narmesteLederRelasjon.orgnummer,
                telefonnummer = narmesteLederRelasjon.narmesteLederTelefonnummer,
                aktivFom = narmesteLederRelasjon.aktivFom,
                arbeidsgiverForskutterer = narmesteLederRelasjon.arbeidsgiverForskutterer,
                navn = getName(lederPerson),
                fnr = lederPerson.fnr
            )
        }
    }

    fun getName(person: Person): String {
        return if (person.mellomnavn == null) {
            "${person.fornavn} ${person.etternavn}"
        } else {
            "${person.fornavn} ${person.mellomnavn} ${person.etternavn}"
        }
    }
}
