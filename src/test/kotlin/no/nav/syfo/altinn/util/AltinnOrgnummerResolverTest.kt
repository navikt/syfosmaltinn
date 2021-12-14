package no.nav.syfo.altinn.util

import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookupFacotry
import org.amshove.kluent.shouldBeEqualTo
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnOrgnummerResolverTest : Spek({

    val testOverride = "910067494"

    describe("Test OrgnummerResolvers") {
        it("Shuild get testOverride") {
            testOverride shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss")
                .getOrgnummer("other")
            testOverride shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss")
                .getOrgnummer("whitelist4")
        }
        it("should get whitelisted") {
            "811290572" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss")
                .getOrgnummer("811290572")
            "811290742" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss")
                .getOrgnummer("811290742")
            "910975439" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss")
                .getOrgnummer("910975439")
        }
        it("Should get same orgnummer in prod") {
            "1" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("1")
            "2" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("2")
            "3" shouldBeEqualTo AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("3")
        }
    }
})
