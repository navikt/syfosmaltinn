package no.nav.syfo.narmesteleder.kafka

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.kafka.model.NarmestelederLeesah
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

class NarmestelederConsumer(
    private val narmestelederDB: NarmestelederDB,
    private val kafkaConsumer: KafkaConsumer<String, NarmestelederLeesah>,
    private val narmestelederTopic: String,
    private val applicationState: ApplicationState
) {
    companion object {
        private val log = LoggerFactory.getLogger(NarmestelederConsumer::class.java)
    }

    @DelicateCoroutinesApi
    fun startConsumer() {
        GlobalScope.launch(Dispatchers.IO) {
            while (applicationState.ready) {
                try {
                    start()
                } catch (ex: Exception) {
                    log.error("Error running kafka consumer, unsubscribing and waiting 10 seconds for retry", ex)
                    kafkaConsumer.unsubscribe()
                    delay(10_000)
                }
            }
        }
    }

    private fun start() {
        kafkaConsumer.subscribe(listOf(narmestelederTopic))
        while (applicationState.ready) {
            kafkaConsumer.poll(Duration.ofMillis(10_000)).map { it.value() }.forEach {
                when (it.aktivTom) {
                    null -> narmestelederDB.insertOrUpdate(it)
                    else -> narmestelederDB.deleteNarmesteleder(it)
                }
            }
        }
    }
}
