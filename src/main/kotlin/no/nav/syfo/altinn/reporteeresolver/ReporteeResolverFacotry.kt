package no.nav.syfo.altinn.reporteeresolver

class ReporteeResolverFacotry private constructor () {
    companion object {

        private const val DEV_CLUSTER = "dev-fss"

        fun getReporteeResolver(cluster: String): AltinnReporteeResolver {
            return when (cluster) {
                DEV_CLUSTER -> AltinnTestReporteeResolver()
                else -> AltinnProdReporteeResolver()
            }
        }
    }
}
