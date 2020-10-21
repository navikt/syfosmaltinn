package no.nav.syfo.altinn.reportee

import no.nav.syfo.getFileAsString

class AltinnTestReporteeLookup : AltinnReporteeLookup {
    private val overrideReportee: String = getFileAsString("/secrets/vault/altinn_test_overstyr_orgnr")
    private val altinnWhitelistReportee: List<String> = getFileAsString("/secrets/vault/altinn_test_whitelist_orgnr").split(",")

    override fun getReportee(orgnummer: String): String {
        return when (altinnWhitelistReportee.contains(orgnummer)) {
            true -> orgnummer
            else -> overrideReportee
        }
    }
}
