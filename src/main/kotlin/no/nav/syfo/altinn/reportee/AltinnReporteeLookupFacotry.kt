package no.nav.syfo.altinn.reportee

class AltinnReporteeLookupFacotry private constructor () {
    companion object {

        private const val DEV_CLUSTER = listOf("dev-fss", "dev-gcp")

        fun getReporteeResolver(cluster: String): AltinnReporteeLookup {
            return when (DEV_CLUSTER.contains(cluster)) {
                true -> AltinnTestReporteeLookup()
                else -> AltinnProdReporteeLookup()
            }
        }
    }
}
