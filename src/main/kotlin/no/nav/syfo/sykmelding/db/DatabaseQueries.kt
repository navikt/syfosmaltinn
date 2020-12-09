package no.nav.syfo.sykmelding.db

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun DatabaseInterface.getStatus(id: String): SykmeldingStatus? {
    return connection.use { connection ->
        connection.prepareStatement("""
           SELECT * from status where sykmelding_id = ?
        """).use {
            it.setString(1, id)
            it.executeQuery().toSykmeldingStatus()
        }
    }
}

fun DatabaseInterface.insertStatus(id: String) {
    connection.use { connection ->
        connection.prepareStatement("""
       INSERT INTO status(sykmelding_id) values (?)
    """).use { ps ->
            ps.setString(1, id)
            ps.executeUpdate()
        }
        connection.commit()
    }
}

fun DatabaseInterface.updateSendtToAlinn(id: String, now: OffsetDateTime) {
    connection.use { connection ->
        connection.prepareStatement("""
           UPDATE status set altinn_timestamp = ? where sykmelding_id = ?
        """).use { ps ->
            ps.setTimestamp(1, Timestamp.from(now.toInstant()))
            ps.setString(2, id)
            ps.executeUpdate()
        }
        connection.commit()
    }
}

fun DatabaseInterface.updateSendtToLogg(id: String, now: OffsetDateTime) {
    connection.use { connection ->
        connection.prepareStatement("""
           UPDATE status set logg_timestamp = ? where sykmelding_id = ?
        """).use { ps ->
            ps.setTimestamp(1, Timestamp.from(now.toInstant()))
            ps.setString(2, id)
            ps.executeUpdate()
        }
        connection.commit()
    }
}

private fun ResultSet.toSykmeldingStatus(): SykmeldingStatus? {
    return when (next()) {
        true -> SykmeldingStatus(getString("sykmelding_id"),
            getTimestamp("altinn_timestamp")?.toInstant()?.atOffset(ZoneOffset.UTC),
            getTimestamp("logg_timestamp")?.toInstant()?.atOffset(ZoneOffset.UTC))
        false -> null
    }
}
