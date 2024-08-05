package no.nav.syfo.altinn.model

data class AltinnStatus(
    val correspondenceId: String,
    val createdDate: String,
    val orgnummer: String,
    val sendersReference: String,
    val statusChanges: Set<StatusChanges>,
)

data class StatusChanges(
    val date: String,
    val type: String,
)
