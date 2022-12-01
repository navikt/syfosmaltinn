package no.nav.syfo.altinn.model

import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType.EMAIL
import no.altinn.schemas.services.serviceengine.notification._2009._10.Notification
import no.altinn.schemas.services.serviceengine.notification._2009._10.NotificationBEList
import no.altinn.schemas.services.serviceengine.notification._2009._10.ReceiverEndPoint
import no.altinn.schemas.services.serviceengine.notification._2009._10.ReceiverEndPointBEList
import no.altinn.schemas.services.serviceengine.notification._2009._10.TextToken
import no.altinn.schemas.services.serviceengine.notification._2009._10.TextTokenSubstitutionBEList

class NotificationAltinnGenerator private constructor() {
    companion object {
        private const val NORSK_BOKMAL = "1044"
        private const val FRA_EPOST_ALTINN = "noreply@altinn.no"

        fun createNotifications(): NotificationBEList {
            return NotificationBEList()
                .withNotification(epostNotification())
        }

        private fun createEmailNotification(vararg text: String): Notification {
            return createNotification(convertToTextTokens(*text))
        }

        private fun epostNotification(): Notification? {
            return createEmailNotification(
                "Ny sykmelding i Altinn",
                "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en digital sykmelding.</p>" +
                    "<p>Logg inn på Altinn for å se sykmeldingen.</p>" +
                    "<p>Husk samtidig å melde inn hvem som er nærmeste leder for den sykmeldte hvis dette ikke er gjort tidligere.</p>" +
                    "<p>Vennlig hilsen NAV.</p>"
            )
        }

        private fun createNotification(textTokens: Array<TextToken?>): Notification {
            if (textTokens.size != 2) {
                throw IllegalArgumentException("Antall textTokens må være 2. Var ${textTokens.size}")
            }
            return Notification()
                .withLanguageCode(
                    NORSK_BOKMAL
                )
                .withNotificationType(
                    "TokenTextOnly"
                )
                .withFromAddress(
                    FRA_EPOST_ALTINN
                )
                .withReceiverEndPoints(
                    ReceiverEndPointBEList()
                        .withReceiverEndPoint(
                            ReceiverEndPoint()
                                .withTransportType(
                                    EMAIL
                                )
                        )
                )
                .withTextTokens(
                    TextTokenSubstitutionBEList().withTextToken(
                        *textTokens
                    )
                )
        }

        private fun convertToTextTokens(vararg text: String): Array<TextToken?> {
            val textTokens = arrayOfNulls<TextToken>(text.size)
            for (i in text.indices) {
                textTokens[i] = TextToken().withTokenNum(i).withTokenValue(
                    text[i]
                )
            }
            return textTokens
        }
    }
}
