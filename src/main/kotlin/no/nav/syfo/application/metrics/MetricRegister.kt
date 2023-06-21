package no.nav.syfo.application.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

const val METRICS_NS = "syfosmaltinn"

val HTTP_HISTOGRAM: Histogram =
    Histogram.Builder()
        .labelNames("path")
        .name("requests_duration_seconds")
        .help("http requests durations for incoming requests in seconds")
        .register()

val ALTINN_COUNTER: Counter =
    Counter.Builder()
        .namespace(METRICS_NS)
        .help("Antall sykmeldinger sendt til Altinn")
        .name("sendt_to_altinn")
        .register()

val JURIDISK_LOGG_COUNTER: Counter =
    Counter.Builder()
        .namespace(METRICS_NS)
        .help("Antall sykmeldinger sendt til juridisk logg")
        .name("sendt_to_juridisk_logg")
        .register()
