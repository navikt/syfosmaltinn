package no.nav.syfo.sykmelding.db

import io.mockk.every
import io.mockk.mockk
import no.nav.syfo.Environment
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.db.NarmestelederDbModel
import no.nav.syfo.narmesteleder.kafka.model.NarmestelederLeesah
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertFailsWith

class PsqlContainer : PostgreSQLContainer<PsqlContainer>("postgres:12")

class DatabaseTest : Spek({
    val mockEnv = mockk<Environment>(relaxed = true)
    every { mockEnv.databaseUsername } returns "username"
    every { mockEnv.databasePassword } returns "password"

    val psqlContainer = PsqlContainer()
        .withExposedPorts(5432)
        .withUsername("username")
        .withPassword("password")
        .withDatabaseName("databasename1")
        .withInitScript("db/testdb-init.sql")

    psqlContainer.start()

    beforeEachTest {
        every { mockEnv.databaseUsername } returns "username"
        every { mockEnv.databasePassword } returns "password"
    }

    describe("Test NL database") {
        it("test save, update and delete") {
            every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
            val database = Database(mockEnv)
            val nlDatabase = NarmestelederDB(database)

            val narmesteleder = NarmestelederLeesah(UUID.randomUUID(), "1", "orgnummer", "2", "telefon", "epost", LocalDate.of(2021, 1, 1), null, null, OffsetDateTime.now())
            nlDatabase.insertOrUpdate(narmesteleder)
            nlDatabase.getNarmesteleder("1", "orgnummer") shouldEqual NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "epost",
                narmesteLederTelefonnummer = "telefon",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = null
            )

            nlDatabase.insertOrUpdate(narmesteleder.copy(narmesteLederEpost = "ny-epost", narmesteLederTelefonnummer = "ny-telefon", aktivFom = LocalDate.of(2021, 2, 1), arbeidsgiverForskutterer = true))
            nlDatabase.getNarmesteleder("1", "orgnummer") shouldEqual NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "ny-epost",
                narmesteLederTelefonnummer = "ny-telefon",
                aktivFom = LocalDate.of(2021, 2, 1),
                arbeidsgiverForskutterer = true
            )
            nlDatabase.insertOrUpdate(narmesteleder.copy(arbeidsgiverForskutterer = false))
            nlDatabase.getNarmesteleder("1", "orgnummer") shouldEqual NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "epost",
                narmesteLederTelefonnummer = "telefon",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = false
            )
            nlDatabase.deleteNarmesteleder(narmesteleder)
            nlDatabase.getNarmesteleder("1", "orgnummer") shouldEqual null
        }
    }

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

    describe("Test db queries") {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)

        it("Lagre narmesteleder check") {
            database.hasCheckedNl("1") shouldEqual false
            database.insertNarmestelederCheck("1", OffsetDateTime.now())
            database.hasCheckedNl("1") shouldEqual true
        }

        it("Insert new sykmeldingStatus") {
            database.insertStatus("1")
            val status = database.getStatus("1")
            status!!.sykmeldingId shouldEqual "1"
            status!!.altinnTimestamp shouldEqual null
            status!!.loggTimestamp shouldEqual null
        }

        it("Insert new sykmeldingStatus and altinn timestamp") {
            val dateTime = OffsetDateTime.now(ZoneOffset.UTC)
            database.insertStatus("2")
            database.updateSendtToAlinn("2", dateTime)
            val status = database.getStatus("2")
            status!!.sykmeldingId shouldEqual "2"
            status!!.altinnTimestamp shouldEqual dateTime
            status!!.loggTimestamp shouldEqual null
        }
        it("Insert new sykmeldingStatus and timestamps") {
            val dateTime = OffsetDateTime.now(ZoneOffset.UTC)
            database.insertStatus("3")
            database.updateSendtToAlinn("3", dateTime)
            database.updateSendtToLogg("3", dateTime)
            val status = database.getStatus("3")
            status!!.sykmeldingId shouldEqual "3"
            status!!.altinnTimestamp shouldEqual dateTime
            status!!.loggTimestamp shouldEqual dateTime
        }
    }
})
