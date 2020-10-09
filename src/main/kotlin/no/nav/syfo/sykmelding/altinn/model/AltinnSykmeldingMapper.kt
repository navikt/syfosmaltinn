package no.nav.syfo.sykmelding.altinn.model

import java.io.StringWriter
import java.lang.Boolean.FALSE
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.AttachmentsV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.UserTypeRestriction
import no.altinn.schemas.services.serviceengine.subscription._2009._10.AttachmentFunctionType
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentExternalBEV2List
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentV2
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPasient
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPeriode
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmelding
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import no.nav.syfo.sykmelding.altinn.util.JAXB
import no.nav.syfo.sykmelding.altinn.util.JAXB.Companion.parseXml
import org.w3c.dom.Document
import org.w3c.dom.Element

class AltinnSykmeldingMapper private constructor() {
    companion object {
        private const val SYKMELDING_TJENESTEKODE =
            "4503" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykmelding i Altinn!
        private const val SYKMELDING_TJENESTEVERSJON = "2"
        private const val NORSK_BOKMAL = "1044"

        private const val NAMESPACE = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        private const val BINARY_NAMESPACE = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10"
        private const val NARMESTE_LEDER_TAG_NAME = "naermesteLeder"

        fun sykmeldingTilCorrespondence(
            sykmeldingAltinn: SykmeldingAltinn,
            brukernavn: String
        ): InsertCorrespondenceV2 {

            val insertCorrespondenceV2 = InsertCorrespondenceV2()
                .withAllowForwarding(JAXBElement(QName(NAMESPACE, "AllowForwarding"), Boolean::class.javaObjectType, FALSE))
                .withReportee(
                    JAXBElement(
                        QName(NAMESPACE, "Reportee"),
                        String::class.java,
                        sykmeldingAltinn.xmlSykmeldingArbeidsgiver.virksomhetsnummer
                    )
                )
                .withMessageSender(
                    JAXBElement(
                        QName(NAMESPACE, "MessageSender"),
                        String::class.java,
                        getFormatetUsername(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmelding.pasient, brukernavn)
                    )
                )
                .withServiceCode(
                    JAXBElement(
                        QName(NAMESPACE, "ServiceCode"),
                        String::class.java,
                        SYKMELDING_TJENESTEKODE
                    )
                )
                .withServiceEdition(
                    JAXBElement(
                        QName(NAMESPACE, "ServiceEdition"),
                        String::class.java,
                        SYKMELDING_TJENESTEVERSJON
                    )
                )
                .withNotifications(NotificationAltinnGenerator.createNotifications(NAMESPACE))
                .withContent(
                    JAXBElement(
                        QName(NAMESPACE, "Content"), ExternalContentV2::class.java, ExternalContentV2()
                            .withLanguageCode(
                                JAXBElement(
                                    QName(NAMESPACE, "LanguageCode"),
                                    String::class.java,
                                    NORSK_BOKMAL
                                )
                            )
                            .withMessageTitle(
                                JAXBElement(
                                    QName(NAMESPACE, "MessageTitle"),
                                    String::class.java,
                                    createTitle(sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmelding, brukernavn)
                                )
                            )
                            .withMessageBody(
                                JAXBElement(
                                    QName(NAMESPACE, "MessageBody"),
                                    String::class.java,
                                    sykmeldingAltinn.sykmeldingPortableHTML
                                )
                            )
                            .withCustomMessageData(null)
                            .withAttachments(
                                JAXBElement(
                                    QName(NAMESPACE, "Attachments"), AttachmentsV2::class.java, AttachmentsV2()
                                        .withBinaryAttachments(
                                            JAXBElement(
                                                QName(NAMESPACE, "BinaryAttachments"),
                                                BinaryAttachmentExternalBEV2List::class.java,
                                                BinaryAttachmentExternalBEV2List()
                                                    .withBinaryAttachmentV2(
                                                        createBinaryAttachment(
                                                            BINARY_NAMESPACE,
                                                            sykmeldingAltinn.sykmeldingPdf,
                                                            "sykmelding.pdf",
                                                            "sykmelding",
                                                            sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId + ".pdf"
                                                        ),
                                                        createBinaryAttachment(
                                                            BINARY_NAMESPACE,
                                                            sykmeldingAltinn.sykmeldingXml.toByteArray(),
                                                            "sykmelding.xml",
                                                            "sykmelding",
                                                            sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmeldingId + ".xml"
                                                        )

                                                    )
                                            )
                                        )
                                )
                            )
                    )
                ).withArchiveReference(null)
            return insertCorrespondenceV2
        }

        private fun createBinaryAttachment(
            binaryNamespace: String,
            fil: ByteArray,
            filnavn: String,
            navn: String,
            sendersRef: String
        ): BinaryAttachmentV2? {
            return BinaryAttachmentV2()
                .withDestinationType(UserTypeRestriction.SHOW_TO_ALL)
                .withFileName(
                    JAXBElement(
                        QName(binaryNamespace, "FileName"),
                        String::class.java, filnavn
                    )
                )
                .withName(
                    JAXBElement(
                        QName(binaryNamespace, "Name"),
                        String::class.java, navn
                    )
                )
                .withFunctionType(AttachmentFunctionType.UNSPECIFIED)
                .withEncrypted(false)
                .withSendersReference(
                    JAXBElement(
                        QName(binaryNamespace, "SendersReference"),
                        String::class.java, sendersRef
                    )
                )
                .withData(
                    JAXBElement(
                        QName("http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10", "Data"),
                        ByteArray::class.java, fil
                    )
                )
        }

        private fun createTitle(sykmelding: XMLSykmelding, brukernavn: String): String {
            val fnr = sykmelding.pasient.ident
            return "Sykmelding - ${periodeAsText(sykmelding.perioder)} - $brukernavn ($fnr)"
        }

        private fun periodeAsText(perioder: List<XMLPeriode>): String {
            val firstFom =
                perioder.minBy(XMLPeriode::getFom)?.fom ?: throw RuntimeException("Sykmelding mangler perioder!")
            val lastTom =
                perioder.maxBy(XMLPeriode::getTom)?.tom ?: throw RuntimeException("Sykmelding mangler perioder!")

            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            return "${dateTimeFormatter.format(firstFom)}-${dateTimeFormatter.format(lastTom)}"
        }

        private fun getFormatetUsername(pasient: XMLPasient?, brukernavn: String): String {
            val fnr = pasient?.ident
            return "$brukernavn - $fnr"
        }

        fun appendNaermesteLeder(document: Document, narmesteLeder: NarmesteLeder) {
            val element = document.createElement(NARMESTE_LEDER_TAG_NAME)

            appendElement(document, element, narmesteLeder.navn, "navn")
            appendElement(document, element, narmesteLeder.fnr, "fnr")
            appendElement(document, element, narmesteLeder.telefonnummer, "mobil")
            appendElement(document, element, narmesteLeder.epost, "epost")
            appendElement(document, element, narmesteLeder.aktivFom.format(DateTimeFormatter.ISO_LOCAL_DATE), "fom")

            document.firstChild.appendChild(element)
        }

        fun appendElement(document: Document, parent: Element, data: String, elementNavn: String) {
            val element = document.createElement(elementNavn)
            element.textContent = data
            parent.appendChild(element)
        }

        fun toSykmeldingXml(
            narmesteLeder: NarmesteLeder?,
            stillingsprosent: Int?,
            xmlSykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver
        ): String {
            val sykmeldingXml = JAXB.marshallSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver)
            if (narmesteLeder != null || stillingsprosent != null) {
                return appendToXml(narmesteLeder, stillingsprosent, sykmeldingXml)
            }
            return sykmeldingXml
        }

        fun appendToXml(narmesteLeder: NarmesteLeder?, stillingsprosent: Int?, sykmeldingXML: String): String {
            val document: Document = parseXml(sykmeldingXML)
            if (narmesteLeder != null) {
                appendNaermesteLeder(document, narmesteLeder)
            }
            if (stillingsprosent != null) {
                appendStillingsprosentToXml(stillingsprosent, document)
            }
            return xmlToString(document)
        }

        fun appendStillingsprosentToXml(stillingsprosent: Int, document: Document) {
            val element = document.getElementsByTagName("arbeidsgiver").item(0) as Element?
            if (element != null) {
                appendElement(document, element, "$stillingsprosent % stilling", "stillingsprosent")
            }
        }

        fun xmlToString(doc: Document?): String {
            return try {
                val sw = StringWriter()
                val tf = TransformerFactory.newInstance()
                val transformer = tf.newTransformer()
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
                transformer.setOutputProperty(OutputKeys.METHOD, "xml")
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                transformer.transform(DOMSource(doc), StreamResult(sw))
                sw.toString()
            } catch (ex: Exception) {
                throw RuntimeException("Error converting to String", ex)
            }
        }
    }
}
