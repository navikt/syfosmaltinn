package no.nav.syfo.narmesteleder.db

import java.sql.Date
import java.sql.ResultSet
import no.nav.syfo.narmesteleder.kafka.model.NarmestelederLeesah
import no.nav.syfo.sykmelding.db.DatabaseInterface

class NarmestelederDB(
    private val database: DatabaseInterface
) {
    fun insertOrUpdate(narmesteleder: NarmestelederLeesah) {
        database.connection.use { connection ->
            connection.prepareStatement(
                """
               insert into narmesteleder(narmeste_leder_id, pasient_fnr, leder_fnr, orgnummer, epost, telefon, fom) 
               values (?, ?, ?, ?, ?, ?, ?) on conflict (narmeste_leder_id) do update 
                set epost = ?,
                    telefon = ?,
                    fom = ?;
            """
            ).use { preparedStatement ->
                // Insert
                preparedStatement.setString(1, narmesteleder.narmesteLederId.toString())
                preparedStatement.setString(2, narmesteleder.fnr)
                preparedStatement.setString(3, narmesteleder.narmesteLederFnr)
                preparedStatement.setString(4, narmesteleder.orgnummer)
                preparedStatement.setString(5, narmesteleder.narmesteLederEpost)
                preparedStatement.setString(6, narmesteleder.narmesteLederTelefonnummer)
                preparedStatement.setDate(7, Date.valueOf(narmesteleder.aktivFom))
                // update
                preparedStatement.setString(8, narmesteleder.narmesteLederEpost)
                preparedStatement.setString(9, narmesteleder.narmesteLederTelefonnummer)
                preparedStatement.setDate(10, Date.valueOf(narmesteleder.aktivFom))
                preparedStatement.executeUpdate()
            }
            connection.commit()
        }
    }

    fun deleteNarmesteleder(narmesteleder: NarmestelederLeesah) {
        database.connection.use { connection ->
            connection.prepareStatement(
                """
               delete from narmesteleder where narmeste_leder_id = ?;
            """
            ).use { ps ->
                ps.setString(1, narmesteleder.narmesteLederId.toString())
                ps.executeUpdate()
            }
            connection.commit()
        }
    }

    fun getNarmesteleder(sykmeldtFnr: String, orgnummer: String): NarmestelederDbModel? {
        return database.connection.use { connection ->
            connection.prepareStatement("""select * from narmesteleder where pasient_fnr = ? and orgnummer = ?;""").use { ps ->
                ps.setString(1, sykmeldtFnr)
                ps.setString(2, orgnummer)
                ps.executeQuery().toNarmestelederDbModel()
            }
        }
    }
}

private fun ResultSet.toNarmestelederDbModel(): NarmestelederDbModel? {
    return when (next()) {
        false -> null
        else -> NarmestelederDbModel(
            sykmeldtFnr = getString("pasient_fnr"),
            lederFnr = getString("leder_fnr"),
            narmesteLederTelefonnummer = getString("telefon"),
            narmesteLederEpost = getString("epost"),
            orgnummer = getString("orgnummer"),
            aktivFom = getDate("fom").toLocalDate()
        )
    }
}
