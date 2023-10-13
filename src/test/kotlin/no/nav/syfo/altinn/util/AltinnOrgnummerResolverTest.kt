package no.nav.syfo.altinn.util

import no.nav.syfo.altinn.orgnummer.AltinnOrgnummerLookupFacotry
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AltinnOrgnummerResolverTest {
    val testOverride = "910067494"

    @Test
    internal fun `Test OrgnummerResolvers Should get testOverride`() {
        testOverride shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("other")
        testOverride shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("whitelist4")
    }

    @Test
    internal fun `Test OrgnummerResolvers should get whitelisted`() {
        "811290572" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("811290572")
        "811290742" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("811290742")
        "910975439" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("dev-fss").getOrgnummer("910975439")
    }

    @Test
    internal fun `Test OrgnummerResolversShould get same orgnummer in prod`() {
        "1" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("1")
        "2" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("2")
        "3" shouldBeEqualTo
            AltinnOrgnummerLookupFacotry.getOrgnummerResolver("prod-fss").getOrgnummer("3")
    }
}
