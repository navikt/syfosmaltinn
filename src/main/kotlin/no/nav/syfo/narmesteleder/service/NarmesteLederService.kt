package no.nav.syfo.narmesteleder.service

import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.pdl.client.PdlClient
import no.nav.syfo.pdl.client.model.Person

class NarmesteLederService(
    private val narmestelederDB: NarmestelederDB,
    private val pdlClient: PdlClient,
) {
    suspend fun getNarmesteLeder(orgnummer: String, fnr: String): NarmesteLeder? {
        return narmestelederDB.getNarmesteleder(
            sykmeldtFnr = fnr,
            orgnummer = orgnummer,
        )?.let { narmesteLederRelasjon ->

            val lederPerson = pdlClient.getPerson(narmesteLederRelasjon.lederFnr)

            NarmesteLeder(
                epost = narmesteLederRelasjon.narmesteLederEpost,
                orgnummer = narmesteLederRelasjon.orgnummer,
                telefonnummer = narmesteLederRelasjon.narmesteLederTelefonnummer,
                aktivFom = narmesteLederRelasjon.aktivFom,
                arbeidsgiverForskutterer = narmesteLederRelasjon.arbeidsgiverForskutterer,
                navn = getName(lederPerson),
                fnr = lederPerson.fnr,
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
