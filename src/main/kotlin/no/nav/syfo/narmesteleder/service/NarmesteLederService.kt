package no.nav.syfo.narmesteleder.service

import io.ktor.util.KtorExperimentalAPI
import no.nav.syfo.client.StsOidcClient
import no.nav.syfo.narmesteleder.client.NarmestelederClient
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person

class NarmesteLederService @KtorExperimentalAPI constructor(private val narmestelederClient: NarmestelederClient, private val pdlClient: PdlClient, private val stsOidcClient: StsOidcClient) {
    suspend fun getNarmesteLeder(orgnummer: String, aktorId: String): NarmesteLeder? {
        return narmestelederClient.getNarmesteleder(
            orgnummer,
            aktorId
        ).narmesteLederRelasjon?.let { narmesteLederRelasjon ->

            val person = pdlClient.getPerson(narmesteLederRelasjon.narmesteLederAktorId, stsOidcClient.oidcToken().access_token)

            NarmesteLeder(
                aktorId = narmesteLederRelasjon.narmesteLederAktorId,
                epost = narmesteLederRelasjon.narmesteLederEpost,
                orgnummer = narmesteLederRelasjon.orgnummer,
                telefonnummer = narmesteLederRelasjon.narmesteLederTelefonnummer,
                aktivFom = narmesteLederRelasjon.aktivFom,
                arbeidsgiverForskutterer = narmesteLederRelasjon.arbeidsgiverForskutterer,
                skrivetilgang = narmesteLederRelasjon.skrivetilgang,
                tilganger = narmesteLederRelasjon.tilganger,
                navn = getName(person),
                fnr = person.fnr
            )
        }
        return null
    }

    private fun getName(person: Person): String {
        return "${person.fornavn} ${person.mellomnavn?.let { "$it " }}${person.etternavn}"
    }
}
