package no.nav.syfo.altinn.model

import java.io.StringWriter
import java.lang.Boolean.FALSE
import java.time.format.DateTimeFormatter
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
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLPasient
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLPeriode
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmelding
import no.nav.helse.xml.sykmelding.arbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.altinn.util.JAXB
import no.nav.syfo.altinn.util.JAXB.Companion.parseXml
import no.nav.syfo.exception.AltinnException
import no.nav.syfo.narmesteleder.model.NarmesteLeder
import org.w3c.dom.Document
import org.w3c.dom.Element

class AltinnSykmeldingMapper private constructor() {
    companion object {
        const val SYKMELDING_TJENESTEKODE =
            "4503" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til
        // sykmelding i Altinn!
        private const val SYKMELDING_TJENESTEVERSJON = "2"
        private const val NORSK_BOKMAL = "1044"

        private const val NARMESTE_LEDER_TAG_NAME = "naermesteLeder"

        fun sykmeldingTilCorrespondence(
            sykmeldingAltinn: SykmeldingAltinn,
            brukernavn: String,
            orgnummer: String,
        ): InsertCorrespondenceV2 {
            val insertCorrespondenceV2 =
                InsertCorrespondenceV2()
                    .withAllowForwarding(FALSE)
                    .withReportee(
                        orgnummer,
                    )
                    .withMessageSender(
                        getFormatetUsername(
                            sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmelding.pasient,
                            brukernavn
                        ),
                    )
                    .withServiceCode(
                        SYKMELDING_TJENESTEKODE,
                    )
                    .withServiceEdition(
                        SYKMELDING_TJENESTEVERSJON,
                    )
                    .withNotifications(NotificationAltinnGenerator.createNotifications())
                    .withContent(
                        ExternalContentV2()
                            .withLanguageCode(
                                NORSK_BOKMAL,
                            )
                            .withMessageTitle(
                                createTitle(
                                    sykmeldingAltinn.xmlSykmeldingArbeidsgiver.sykmelding,
                                    brukernavn
                                ),
                            )
                            .withMessageBody(
                                sykmeldingAltinn.sykmeldingPortableHTML,
                            )
                            .withCustomMessageData(null)
                            .withAttachments(
                                AttachmentsV2()
                                    .withBinaryAttachments(
                                        BinaryAttachmentExternalBEV2List()
                                            .withBinaryAttachmentV2(
                                                createBinaryAttachment(
                                                    sykmeldingAltinn.sykmeldingPdf,
                                                    "sykmelding.pdf",
                                                    "sykmelding",
                                                    sykmeldingAltinn.xmlSykmeldingArbeidsgiver
                                                        .sykmeldingId + ".pdf",
                                                ),
                                                createBinaryAttachment(
                                                    sykmeldingAltinn.sykmeldingXml.toByteArray(),
                                                    "sykmelding.xml",
                                                    "sykmelding",
                                                    sykmeldingAltinn.xmlSykmeldingArbeidsgiver
                                                        .sykmeldingId + ".xml",
                                                ),
                                            ),
                                    ),
                            ),
                    )
                    .withArchiveReference(null)
            return insertCorrespondenceV2
        }

        private fun createBinaryAttachment(
            fil: ByteArray,
            filnavn: String,
            navn: String,
            sendersRef: String,
        ): BinaryAttachmentV2? {
            return BinaryAttachmentV2()
                .withDestinationType(UserTypeRestriction.SHOW_TO_ALL)
                .withFileName(
                    filnavn,
                )
                .withName(
                    navn,
                )
                .withFunctionType(AttachmentFunctionType.UNSPECIFIED)
                .withEncrypted(false)
                .withSendersReference(
                    sendersRef,
                )
                .withData(
                    fil,
                )
        }

        private fun createTitle(sykmelding: XMLSykmelding, brukernavn: String): String {
            val fnr = sykmelding.pasient.ident
            return "Sykmelding - ${periodeAsText(sykmelding.perioder)} - $brukernavn ($fnr)"
        }

        private fun periodeAsText(perioder: List<XMLPeriode>): String {
            val firstFom =
                perioder.minByOrNull { it.fom }?.fom
                    ?: throw RuntimeException("Sykmelding mangler perioder!")
            val lastTom =
                perioder.maxByOrNull { it.tom }?.tom
                    ?: throw RuntimeException("Sykmelding mangler perioder!")

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
            appendElement(
                document,
                element,
                narmesteLeder.aktivFom.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "fom"
            )

            document.firstChild.appendChild(element)
        }

        fun appendElement(document: Document, parent: Element, data: String, elementNavn: String) {
            val element = document.createElement(elementNavn)
            element.textContent = data
            parent.appendChild(element)
        }

        fun toSykmeldingXml(
            narmesteLeder: NarmesteLeder?,
            xmlSykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver,
        ): String {
            val sykmeldingXml = JAXB.marshallSykmeldingArbeidsgiver(xmlSykmeldingArbeidsgiver)
            if (narmesteLeder != null) {
                return appendToXml(narmesteLeder, sykmeldingXml)
            }
            return sykmeldingXml
        }

        fun appendToXml(narmesteLeder: NarmesteLeder?, sykmeldingXML: String): String {
            val document: Document = parseXml(sykmeldingXML)
            if (narmesteLeder != null) {
                appendNaermesteLeder(document, narmesteLeder)
            }
            return xmlToString(document)
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
                throw AltinnException("Error converting to String", ex)
            }
        }
    }
}
