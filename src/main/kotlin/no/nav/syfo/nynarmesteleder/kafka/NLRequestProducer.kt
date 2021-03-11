package no.nav.syfo.nynarmesteleder.kafka

import no.nav.syfo.log
import no.nav.syfo.nynarmesteleder.kafka.model.NlRequest
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class NLRequestProducer(private val kafkaProducer: KafkaProducer<String, NlRequest>, private val topicName: String) {
    fun send(nlRequest: NlRequest) {
        try {
            kafkaProducer.send(ProducerRecord(topicName, nlRequest.sykmeldingId, nlRequest)).get()
        } catch (ex: Exception) {
            log.error("Noe gikk galt ved skriving av NL-forespørsel til kafka for sykmeldingid {}, {}", nlRequest.sykmeldingId, ex.message)
            throw ex
        }
    }
}
