package no.nav.syfo.altinn

import no.nav.altinn.admin.ws.configureFor
import no.nav.altinn.admin.ws.stsClient
import no.nav.syfo.altinn.config.createPort
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class AltinnCorrespondenceCreateTest: Spek({
    describe("Altinn") {
        it("test creation") {
            val iCorrespondenceAgencyExternalBasic = createPort("123").apply {
                stsClient("https://sts-q1.preprod.local/SecurityTokenServiceProvider/", "username" to "password").configureFor(this)
            }
        }
    }
})
