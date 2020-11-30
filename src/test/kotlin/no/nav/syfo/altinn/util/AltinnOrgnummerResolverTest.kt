package no.nav.syfo.altinn.util

import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookupFacotry
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnOrgnummerResolverTest : Spek({

    val testOverride = "910067494"

    describe("Test OrgnummerResolvers") {
        it("Shuild get testOverride") {
            testOverride shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("other")
            testOverride shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("whitelist4")
        }
        it("should get whitelisted") {
            "811290572" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("811290572")
            "811290742" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("811290742")
            "910975439" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("910975439")
        }
        it("Should get same orgnummer in prod") {
            "1" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("1")
            "2" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("2")
            "3" shouldEqual AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("3")
        }
    }
})
