package no.nav.syfo.altinn.reporteeresolver

class AltinnProdReporteeResolver : AltinnReporteeResolver {
    override fun getReportee(orgnummer: String): String {
        return orgnummer
    }
}
