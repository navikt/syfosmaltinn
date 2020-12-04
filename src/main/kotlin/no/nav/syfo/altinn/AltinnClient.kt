package no.nav.syfo.altinn

import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.correspondence._2016._02.CorrespondenceStatusFilterV3
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.log

class AltinnClient(
    private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic,
    private val username: String,
    private val password: String
) {
    val SYSTEM_USER_CODE = "NAV_DIGISYFO"
    fun sendToAltinn(insertCorrespondenceV2: InsertCorrespondenceV2, sykmeldingId: String): Int {
        try {
            val receiptExternal = iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
                username,
                password,
                SYSTEM_USER_CODE,
                sykmeldingId,
                insertCorrespondenceV2
            )
            if (receiptExternal.receiptStatusCode != ReceiptStatusEnum.OK) {
                log.error(
                    "Error fra altinn {} for sykmeldingId: {}, {}",
                    receiptExternal.receiptStatusCode,
                    sykmeldingId,
                    receiptExternal.receiptText
                )
                throw RuntimeException("Error from altinn")
            }
            return receiptExternal.receiptId
        } catch (ex: Exception) {
            log.error("Error sending sykmeldign to altinn", ex)
            throw ex
        }
    }

    fun isSendt(id: String, orgnummer: String): Boolean {
        val altinnResponse = iCorrespondenceAgencyExternalBasic.getCorrespondenceStatusDetailsBasicV3(
            username,
            password,
            CorrespondenceStatusFilterV3()
                .withSendersReference(id)
                .withServiceCode(AltinnSykmeldingMapper.SYKMELDING_TJENESTEKODE)
                .withReportee(orgnummer)
                .withServiceEditionCode(2)
        )
        return altinnResponse.correspondenceStatusInformation.correspondenceStatusDetailsList.statusV2.any { it.sendersReference == id }
    }
}
