package no.nav.syfo.altinn.util

import io.mockk.every
import io.mockk.mockkStatic
import no.nav.syfo.altinn.reportee.AltinnReporteeLookupFacotry
import no.nav.syfo.getFileAsString
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnReporteeResolverTest : Spek({

    val testOverride = "testorg"
    val testWithlisted = "whitelist1,whitelist2,whitelist3"

    mockkStatic("no.nav.syfo.EnvironmentKt")
    every { getFileAsString("/secrets/vault/altinn_test_overstyr_orgnr") } returns testOverride
    every { getFileAsString("/secrets/vault/altinn_test_whitelist_orgnr") } returns testWithlisted

    describe("Test ReporteeResolvers") {
        it("Shuild get testOverride") {
            testOverride shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("other")
            testOverride shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("whitelist4")
        }
        it("should get whitelisted") {
            "whitelist1" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("whitelist1")
            "whitelist2" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("whitelist2")
            "whitelist3" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("dev-fss").getReportee("whitelist3")
        }
        it("Should get same orgnummer in prod") {
            "1" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("1")
            "2" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("2")
            "3" shouldEqual AltinnReporteeLookupFacotry.getReporteeResolver("prod-fss").getReportee("3")
        }
    }
})
