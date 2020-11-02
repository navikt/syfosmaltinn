package no.nav.syfo.altinn.util

import no.nav.syfo.altinn.reportee.AltinnReporteeLookupFacotry
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnReporteeResolverTest : Spek({

    val testOverride = "910067494"

    describe("Test ReporteeResolvers") {
        it("Shuild get testOverride") {
            testOverride shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("other")
            testOverride shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("whitelist4")
        }
        it("should get whitelisted") {
            "811290572" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("811290572")
            "811290742" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("811290742")
            "910975439" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("910975439")
        }
        it("Should get same orgnummer in prod") {
            "1" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("1")
            "2" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("2")
            "3" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("3")
        }
    }
})
