package no.nav.syfo.altinn

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.altinn.schemas.services.intermediary.receipt._2009._10.ReceiptStatusEnum
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2
import no.altinn.schemas.services.serviceengine.correspondence._2016._02.CorrespondenceStatusFilterV3
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasic
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicGetCorrespondenceStatusDetailsBasicV3AltinnFaultFaultFaultMessage
import no.altinn.services.serviceengine.correspondence._2009._10.ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage
import no.nav.syfo.altinn.model.AltinnSykmeldingMapper
import no.nav.syfo.helpers.retry
import no.nav.syfo.log
import no.nav.syfo.securelog
import java.io.IOException
import javax.xml.ws.soap.SOAPFaultException

class AltinnClient(
    private val iCorrespondenceAgencyExternalBasic: ICorrespondenceAgencyExternalBasic,
    private val username: String,
    private val password: String,
) {
    val SYSTEM_USER_CODE = "NAV_DIGISYFO"
    val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    }
    suspend fun sendToAltinn(insertCorrespondenceV2: InsertCorrespondenceV2, sykmeldingId: String): Int {
        try {
            securelog.info("insertCorrespondenceV2: ${objectMapper.writeValueAsString(insertCorrespondenceV2)}")
            val receiptExternal = retry(
                callName = "insertCorrespondenceBasicV2",
                retryIntervals = arrayOf(500L, 1000L, 300L),
                legalExceptions = arrayOf(
                    IOException::class,
                    ICorrespondenceAgencyExternalBasicInsertCorrespondenceBasicV2AltinnFaultFaultFaultMessage::class,
                    SOAPFaultException::class,
                ),
            ) {
                iCorrespondenceAgencyExternalBasic.insertCorrespondenceBasicV2(
                    username,
                    password,
                    SYSTEM_USER_CODE,
                    sykmeldingId,
                    insertCorrespondenceV2,
                )
            }
            securelog.info("receipt: ${objectMapper.writeValueAsString(receiptExternal)}")
            if (receiptExternal.receiptStatusCode != ReceiptStatusEnum.OK) {
                log.error(
                    "Error fra altinn {} for sykmeldingId: {}, {}",
                    receiptExternal.receiptStatusCode,
                    sykmeldingId,
                    receiptExternal.receiptText,
                )
                throw RuntimeException("Error from altinn")
            }
            return receiptExternal.receiptId
        } catch (ex: Exception) {
            log.error("Error sending sykmeldign to altinn", ex)
            throw ex
        }
    }

    suspend fun isSendt(id: String, orgnummer: String): Boolean {
        val altinnResponse = retry(
            callName = "getCorrespondenceStatusDetailsBasicV3",
            retryIntervals = arrayOf(500L, 1000L, 300L),
            legalExceptions = arrayOf(
                IOException::class,
                ICorrespondenceAgencyExternalBasicGetCorrespondenceStatusDetailsBasicV3AltinnFaultFaultFaultMessage::class,
                SOAPFaultException::class,
            ),
        ) {
            iCorrespondenceAgencyExternalBasic.getCorrespondenceStatusDetailsBasicV3(
                username,
                password,
                CorrespondenceStatusFilterV3()
                    .withSendersReference(id)
                    .withServiceCode(AltinnSykmeldingMapper.SYKMELDING_TJENESTEKODE)
                    .withReportee(orgnummer)
                    .withServiceEditionCode(2),
            )
        }
        return altinnResponse.correspondenceStatusInformation.correspondenceStatusDetailsList.statusV2.any { it.sendersReference == id }
    }
}
