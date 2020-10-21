package no.nav.syfo.altinn.reportee

class AltinnReporteeLookupFacotry private constructor () {
    companion object {

        private const val DEV_CLUSTER = "dev-fss"

        fun getReporteeResolver(cluster: String): AltinnReporteeLookup {
            return when (cluster) {
                DEV_CLUSTER -> AltinnTestReporteeLookup()
                else -> AltinnProdReporteeLookup()
            }
        }
    }
}
