package no.nav.syfo.narmesteleder.kafka

import no.nav.syfo.exception.AltinnException
import no.nav.syfo.narmesteleder.kafka.model.NlRequestKafkaMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

class NLRequestProducer(
    private val kafkaProducer: KafkaProducer<String, NlRequestKafkaMessage>,
    private val topicName: String
) {
    fun send(nlRequestKafkaMessage: NlRequestKafkaMessage) {
        try {
            kafkaProducer
                .send(
                    ProducerRecord(
                        topicName,
                        nlRequestKafkaMessage.nlRequest.orgnr,
                        nlRequestKafkaMessage
                    )
                )
                .get()
        } catch (ex: Exception) {
            throw AltinnException(
                "Noe gikk galt ved skriving av NL-forespørsel til kafka for sykmeldingid ${nlRequestKafkaMessage.nlRequest.sykmeldingId}",
                ex
            )
        }
    }
}
