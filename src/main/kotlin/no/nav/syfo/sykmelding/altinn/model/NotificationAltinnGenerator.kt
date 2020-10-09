package no.nav.syfo.sykmelding.altinn.model

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType.EMAIL
import no.altinn.schemas.serviceengine.formsengine._2009._10.TransportType.SMS
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
        private const val NOTIFICATION_NAMESPACE =
            "http://schemas.altinn.no/services/ServiceEngine/Notification/2009/10"

        fun createNotifications(namespace: String): JAXBElement<NotificationBEList> {
            return JAXBElement(
                QName(namespace, "Notifications"),
                NotificationBEList::class.java,
                NotificationBEList()
                    .withNotification(epostNotification(), smsNotification())
            )
        }

        fun createEmailNotification(vararg text: String): Notification {
            return createNotification(FRA_EPOST_ALTINN, EMAIL, convertToTextTokens(*text))
        }

        fun createSmsNotification(vararg text: String): Notification {
            return createNotification(null, SMS, convertToTextTokens(*text))
        }

        fun smsLenkeAltinnPortal(): String {
            return urlEncode(lenkeAltinnPortal())
        }

        fun lenkeAltinnPortal(): String {
            return System.getProperty(
                "altinn.portal.baseurl",
                "https://www.altinn.no"
            ) + "/ui/MessageBox?O=\$reporteeNumber$"
        }

        private fun epostNotification(): Notification? {
            return createEmailNotification(
                "Ny sykmelding i Altinn",
                "<p>En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en digital sykmelding.</p>" +
                        "<p><a href=\"" + lenkeAltinnPortal() + "\">" +
                        "Logg inn på Altinn</a> for å se sykmeldingen.</p>" +
                        "<p>Husk samtidig å melde inn hvem som er nærmeste leder for den sykmeldte hvis dette ikke er gjort tidligere.</p>" +
                        "<p>Les mer på <a href=\"https://www.nav.no/digitalsykmelding\">nav.no/digitalsykmelding</a>.</p>" +
                        "<p>Vennlig hilsen NAV.</p>"
            )
        }

        private fun smsNotification(): Notification? {
            return createSmsNotification(
                "En ansatt i \$reporteeName$ (\$reporteeNumber$) har sendt inn en ny sykmelding. ",
                "Gå til " + smsLenkeAltinnPortal().toString() + " for å se sykmeldingen. Vennlig hilsen NAV."
            )
        }

        fun createNotification(fromEmail: String?, type: TransportType, textTokens: Array<TextToken?>): Notification {
            if (textTokens.size != 2) {
                throw IllegalArgumentException("Antall textTokens må være 2. Var ${textTokens.size}")
            }
            return Notification()
                .withLanguageCode(
                    JAXBElement(
                        QName(NOTIFICATION_NAMESPACE, "LanguageCode"),
                        String::class.java,
                        NORSK_BOKMAL
                    )
                )
                .withNotificationType(
                    JAXBElement(
                        QName(NOTIFICATION_NAMESPACE, "NotificationType"),
                        String::class.java,
                        "TokenTextOnly"
                    )
                )
                .withFromAddress(fromEmail?.let {
                    JAXBElement(
                        QName(NOTIFICATION_NAMESPACE, "FromAddress"),
                        String::class.java,
                        it
                    )
                })
                .withReceiverEndPoints(
                    JAXBElement(
                        QName(NOTIFICATION_NAMESPACE, "ReceiverEndPoints"),
                        ReceiverEndPointBEList::class.java,
                        ReceiverEndPointBEList()
                            .withReceiverEndPoint(
                                ReceiverEndPoint()
                                    .withTransportType(
                                        JAXBElement(
                                            QName(NOTIFICATION_NAMESPACE, "TransportType"),
                                            TransportType::class.java,
                                            type
                                        )
                                    )
                            )
                    )
                )
                .withTextTokens(
                    JAXBElement(
                        QName(NOTIFICATION_NAMESPACE, "TextTokens"),
                        TextTokenSubstitutionBEList::class.java,
                        TextTokenSubstitutionBEList().withTextToken(
                            *textTokens
                        )
                    )
                )
        }

        fun urlEncode(lenke: String): String {
            return lenke.replace("=", "%3D")
        }

        private fun convertToTextTokens(vararg text: String): Array<TextToken?> {
            val textTokens = arrayOfNulls<TextToken>(text.size)
            for (i in text.indices) {
                textTokens[i] = TextToken().withTokenNum(i).withTokenValue(
                    JAXBElement(
                        QName(
                            NOTIFICATION_NAMESPACE,
                            "TokenValue"
                        ),
                        String::class.java, text[i]
                    )
                )
            }
            return textTokens
        }
    }
}
