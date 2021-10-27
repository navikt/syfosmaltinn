package no.nav.syfo.sykmelding.kafka.aiven

import java.time.Duration
import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import org.apache.kafka.clients.consumer.KafkaConsumer

class SendtSykmeldingAivenConsumer(
    private val kafkaConsumer: KafkaConsumer<String, SendSykmeldingAivenKafkaMessage>,
    private val topic: String
) {
    fun poll(): List<SendSykmeldingAivenKafkaMessage> {
        return kafkaConsumer.poll(Duration.ofMillis(10_000)).mapNotNull { it.value() }
    }

    fun subscribe() {
        kafkaConsumer.subscribe(listOf(topic))
    }
}
