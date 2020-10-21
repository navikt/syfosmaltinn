package no.nav.syfo.altinn.reportee

interface AltinnReporteeLookup {
    fun getReportee(orgnummer: String): String
}
