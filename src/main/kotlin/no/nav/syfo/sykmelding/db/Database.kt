package no.nav.syfo.sykmelding.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import java.net.ConnectException
import java.net.SocketException
import java.sql.Connection
import no.nav.syfo.Environment
import no.nav.syfo.logger
import org.flywaydb.core.Flyway

class Database(private val env: Environment, retries: Long = 30, sleepTime: Long = 5_000) :
    DatabaseInterface {
    private val dataSource: HikariDataSource
    override val connection: Connection
        get() = dataSource.connection

    init {
        var current = 0
        var connected = false
        var tempDatasource: HikariDataSource? = null
        while (!connected && current++ < retries) {
            logger.info("trying to connet to db current try $current")
            try {
                tempDatasource =
                    HikariDataSource(
                        HikariConfig().apply {
                            jdbcUrl = env.jdbcUrl()
                            username = env.databaseUsername
                            password = env.databasePassword
                            maximumPoolSize = 2
                            minimumIdle = 2
                            idleTimeout = 10000
                            maxLifetime = 300000
                            isAutoCommit = false
                            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                            validate()
                        },
                    )
                connected = true
            } catch (ex: HikariPool.PoolInitializationException) {
                if (ex.cause?.cause is ConnectException || ex.cause?.cause is SocketException) {
                    logger.info("Could not connect to db")
                    Thread.sleep(sleepTime)
                } else {
                    throw ex
                }
            }
        }
        if (tempDatasource == null) {
            logger.error("Could not connect to DB")
            throw RuntimeException("Could not connect to DB")
        }
        dataSource = tempDatasource
        runFlywayMigrations()
    }

    private fun runFlywayMigrations() =
        Flyway.configure().run {
            locations("db/migration")
            dataSource(env.jdbcUrl(), env.databaseUsername, env.databasePassword)
            load().migrate()
        }
}

interface DatabaseInterface {
    val connection: Connection
}
