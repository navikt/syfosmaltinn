package no.nav.syfo.sykmelding.kafka.aiven

import no.nav.syfo.sykmelding.kafka.aiven.model.SendSykmeldingAivenKafkaMessage
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

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
