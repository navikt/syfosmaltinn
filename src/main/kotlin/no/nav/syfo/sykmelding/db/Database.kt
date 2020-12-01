package no.nav.syfo.sykmelding.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.syfo.Environment
import org.flywaydb.core.Flyway
import java.sql.Connection

class Database(private val env: Environment) :
    DatabaseInterface {
    private val dataSource: HikariDataSource

    override val connection: Connection
        get() = dataSource.connection

    init {
        runFlywayMigrations()

        dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = env.databaseUrl
            username = env.databaseUsername
            password = env.databasePassword
            maximumPoolSize = 3
            minimumIdle = 1
            idleTimeout = 10000
            maxLifetime = 300000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })
    }

    private fun runFlywayMigrations() = Flyway.configure().run {
        locations("db")
        dataSource(env.databaseUrl, env.databaseUsername, env.databasePassword)
        load().migrate()
    }
}

interface DatabaseInterface {
    val connection: Connection
}
