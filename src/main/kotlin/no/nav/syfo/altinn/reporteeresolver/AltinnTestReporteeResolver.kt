package no.nav.syfo.altinn.reporteeresolver

import no.nav.syfo.getFileAsString

class AltinnTestReporteeResolver : AltinnReporteeResolver {
    private val overrideReportee: String = getFileAsString("/secrets/vault/altinn_test_overstyr_orgnr")
    private val altinnWhitelistReportee: List<String> = getFileAsString("/secrets/vault/altinn_test_whitelist_orgnr").split(",")

    override fun getReportee(orgnummer: String): String {
        return when (altinnWhitelistReportee.contains(orgnummer)) {
            true -> orgnummer
            else -> overrideReportee
        }
    }
}
