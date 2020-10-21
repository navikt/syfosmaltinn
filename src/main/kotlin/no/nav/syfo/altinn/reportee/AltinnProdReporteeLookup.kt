package no.nav.syfo.altinn.reportee

class AltinnProdReporteeLookup : AltinnReporteeLookup {
    override fun getReportee(orgnummer: String): String {
        return orgnummer
    }
}
