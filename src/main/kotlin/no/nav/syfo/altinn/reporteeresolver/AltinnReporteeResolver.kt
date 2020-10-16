package no.nav.syfo.altinn.reporteeresolver

interface AltinnReporteeResolver {
    fun getReportee(orgnummer: String): String
}
