package no.nav.syfo.altinn.reportee

class AltinnTestReporteeLookup : AltinnReporteeLookup {
    private val overrideReportee: String = AltinnTestReporteeLookup::class.java.getResource("/altinn/altinn_test_overstyr_orgnr").readText().trim()
    private val altinnWhitelistReportee: List<String> = AltinnTestReporteeLookup::class.java.getResource("/altinn/altinn_test_whitelist_orgnr").readText().trim().split(",")

    override fun getReportee(orgnummer: String): String {
        return when (altinnWhitelistReportee.contains(orgnummer)) {
            true -> orgnummer
            else -> overrideReportee
        }
    }
}
