package no.nav.syfo.sykmelding.db

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertFailsWith
import no.nav.syfo.Environment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.testcontainers.containers.PostgreSQLContainer

class PsqlContainer : PostgreSQLContainer<PsqlContainer>()

class DatabaseTest : Spek({
    val mockEnv = mockk<Environment>(relaxed = true)
    every { mockEnv.databaseUsername } returns "username"
    every { mockEnv.databasePassword } returns "password"

    val psqlContainer = PsqlContainer().withExposedPorts(5432).withUsername("username").withPassword("password").withDatabaseName("databasename1")
    psqlContainer.start()

    describe("Test database") {
        it("Should fail 20 times then connect") {
            every { mockEnv.jdbcUrl() } returnsMany (0 until 20).map { "jdbc:postgresql://127.0.0.1:5433/databasename1" } andThen psqlContainer.jdbcUrl
            Database(mockEnv, 30, 0)
        }
        it("Fail after timeout exeeded") {
            every { mockEnv.jdbcUrl() } returns "jdbc:postgresql://127.0.0.1:5433/databasename1"
            assertFailsWith<RuntimeException> { Database(mockEnv, retries = 30, sleepTime = 0) }
        }
    }
})
