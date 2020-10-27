package no.nav.syfo.juridisklogg

import java.security.MessageDigest
import java.util.Base64
import no.nav.syfo.altinn.model.SykmeldingAltinn
import no.nav.syfo.pdl.client.model.Person

class JuridiskLoggService(private val juridiskLoggClient: JuridiskLoggClient) {

    suspend fun sendJuridiskLogg(
        sykmeldingAltinn: SykmeldingAltinn,
        person: Person
    ) {
        val log = Logg(
            meldingsId = sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId,
            antallAarLagres = ANTALL_AR_LAGRES,
            avsender = person.aktorId,
            meldingsInnhold = sha512AsBase64String(sykmeldingAltinn.sykmeldingXml),
            mottaker = sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer
        )
        juridiskLoggClient.logg(log)
    }

    private fun sha512AsBase64String(source: String): String {
        val sha512 = MessageDigest.getInstance("SHA-512")
        val digest = sha512.digest(source.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }

    companion object {
        private const val ANTALL_AR_LAGRES = 5
    }
}
