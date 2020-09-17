package no.nav.syfo.sykmelding.exceptions

import no.nav.syfo.model.sykmeldingstatus.SykmeldingStatusKafkaEventDTO

class ArbeidsgiverNotFoundException(val event: SykmeldingStatusKafkaEventDTO, override val message: String = "Could not get arbeidsgiver from kafka message") : RuntimeException(message)