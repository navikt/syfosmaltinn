package no.nav.syfo.sykmelding.kafka

import java.time.Duration
import no.nav.syfo.sykmelding.kafka.model.SendtSykmeldingKafkaMessage
import org.apache.kafka.clients.consumer.KafkaConsumer

class SendtSykmeldingConsumer(
        private val kafkaConsumer: KafkaConsumer<String, SendtSykmeldingKafkaMessage>,
        private val topic: String
) {
    fun poll(): List<SendtSykmeldingKafkaMessage> {
        return kafkaConsumer.poll(Duration.ofMillis(10_000)).mapNotNull { it.value() }
    }

    fun subscribe() {
        kafkaConsumer.subscribe(listOf(topic))
    }
}
