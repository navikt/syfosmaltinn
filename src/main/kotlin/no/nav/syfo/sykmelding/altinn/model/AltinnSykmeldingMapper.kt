package no.nav.syfo.sykmelding.altinn.model

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLPasient
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver

class AltinnSykmeldingMapper private constructor() {
    companion object {
        private const val SYKMELDING_TJENESTEKODE =
            "4503" // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til sykmelding i Altinn!
        private const val SYKMELDING_TJENESTEVERSJON = "2"
        private const val NORSK_BOKMAL = "1044"

        private const val NAMESPACE = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10"
        private const val BINARY_NAMESPACE = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10"

        fun sykmeldingTilCorrespondence(sykmeldingArbeidsgiver: XMLSykmeldingArbeidsgiver, brukernavn: String): InsertCorrespondenceV2 {
            val insertCorrespondenceV2 = InsertCorrespondenceV2()
                .withAllowForwarding(JAXBElement(QName(NAMESPACE, "AllowForwarding"), Boolean::class.java, false))
                .withReportee(JAXBElement(QName(NAMESPACE, "Reportee"), String::class.java, sykmeldingArbeidsgiver.virksomhetsnummer))
                .withMessageSender(JAXBElement(QName(NAMESPACE, "MessageSender"), String::class.java, getFormatetUsername(sykmeldingArbeidsgiver.sykmelding.pasient, brukernavn)))
                .withServiceCode(JAXBElement(QName(NAMESPACE, "ServiceCode"), String::class.java, SYKMELDING_TJENESTEKODE))
                .withServiceEdition(JAXBElement(QName(NAMESPACE, "ServiceEdition"), String::class.java, SYKMELDING_TJENESTEVERSJON))
                // .withNotifications(JAXBElement(QName(NAMESPACE, )))
            return insertCorrespondenceV2
        }

        private fun getFormatetUsername(pasient: XMLPasient?, brukernavn: String): String {
            val fnr = pasient?.ident
            return "$brukernavn - $fnr"
        }
    }
}
