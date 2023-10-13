package no.nav.syfo.sykmelding.db

import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertFailsWith
import no.nav.syfo.Environment
import no.nav.syfo.narmesteleder.db.NarmestelederDB
import no.nav.syfo.narmesteleder.db.NarmestelederDbModel
import no.nav.syfo.narmesteleder.kafka.model.NarmestelederLeesah
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer

class PsqlContainer : PostgreSQLContainer<PsqlContainer>("postgres:12")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DatabaseTest {
    private val mockEnv = mockk<Environment>(relaxed = true)

    val psqlContainer =
        PsqlContainer()
            .withExposedPorts(5432)
            .withUsername("username")
            .withPassword("password")
            .withDatabaseName("databasename1")
            .withInitScript("db/testdb-init.sql")
            .withCommand("postgres", "-c", "wal_level=logical")

    @BeforeAll
    fun beforeAll() {
        psqlContainer.start()
    }

    @BeforeEach
    fun beforeEach() {
        every { mockEnv.databaseUsername } returns "username"
        every { mockEnv.databasePassword } returns "password"
    }

    @Test
    internal fun `Test NL database test save, update and delete`() {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)
        val nlDatabase = NarmestelederDB(database)

        val narmesteleder =
            NarmestelederLeesah(
                UUID.randomUUID(),
                "1",
                "orgnummer",
                "2",
                "telefon",
                "epost",
                LocalDate.of(2021, 1, 1),
                null,
                null,
                OffsetDateTime.now(),
            )
        nlDatabase.insertOrUpdate(narmesteleder)
        nlDatabase.getNarmesteleder("1", "orgnummer") shouldBeEqualTo
            NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "epost",
                narmesteLederTelefonnummer = "telefon",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = null,
            )

        nlDatabase.insertOrUpdate(
            narmesteleder.copy(
                narmesteLederEpost = "ny-epost",
                narmesteLederTelefonnummer = "ny-telefon",
                aktivFom = LocalDate.of(2021, 2, 1),
                arbeidsgiverForskutterer = true,
            ),
        )
        nlDatabase.getNarmesteleder("1", "orgnummer") shouldBeEqualTo
            NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "ny-epost",
                narmesteLederTelefonnummer = "ny-telefon",
                aktivFom = LocalDate.of(2021, 2, 1),
                arbeidsgiverForskutterer = true,
            )
        nlDatabase.insertOrUpdate(narmesteleder.copy(arbeidsgiverForskutterer = false))
        nlDatabase.getNarmesteleder("1", "orgnummer") shouldBeEqualTo
            NarmestelederDbModel(
                sykmeldtFnr = "1",
                orgnummer = "orgnummer",
                lederFnr = "2",
                narmesteLederEpost = "epost",
                narmesteLederTelefonnummer = "telefon",
                aktivFom = LocalDate.of(2021, 1, 1),
                arbeidsgiverForskutterer = false,
            )
        nlDatabase.deleteNarmesteleder(narmesteleder)
        nlDatabase.getNarmesteleder("1", "orgnummer") shouldBeEqualTo null
    }

    @Test
    internal fun `Test database Should fail 20 times then connect`() {
        every { mockEnv.jdbcUrl() } returnsMany
            (0 until 20).map { "jdbc:postgresql://127.0.0.1:5433/databasename1" } andThen
            psqlContainer.jdbcUrl
        Database(mockEnv, 30, 0)
    }

    @Test
    internal fun `Test database fail after timeout exeeded`() {
        every { mockEnv.jdbcUrl() } returns "jdbc:postgresql://127.0.0.1:5433/databasename1"
        assertFailsWith<RuntimeException> { Database(mockEnv, retries = 30, sleepTime = 0) }
    }

    @Test
    internal fun `Test db queries Lagre narmesteleder check`() {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)

        database.hasCheckedNl("1") shouldBeEqualTo false
        database.insertNarmestelederCheck("1", OffsetDateTime.now())
        database.hasCheckedNl("1") shouldBeEqualTo true
    }

    @Test
    internal fun `Test db queries Insert new sykmeldingStatus`() {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)
        database.insertStatus("1")
        val status = database.getStatus("1")
        status!!.sykmeldingId shouldBeEqualTo "1"
        status.altinnTimestamp shouldBeEqualTo null
        status.loggTimestamp shouldBeEqualTo null
    }

    @Test
    internal fun `Test db queries Insert new sykmeldingStatus and altinn timestamp`() {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)
        val dateTime = OffsetDateTime.now(Clock.tickMillis(ZoneOffset.UTC))
        database.insertStatus("2")
        database.updateSendtToAlinn("2", dateTime)
        val status = database.getStatus("2")
        status!!.sykmeldingId shouldBeEqualTo "2"
        status.altinnTimestamp shouldBeEqualTo dateTime
        status.loggTimestamp shouldBeEqualTo null
    }

    @Test
    internal fun `Test db queries Insert new sykmeldingStatus and timestamps`() {
        every { mockEnv.jdbcUrl() } returns psqlContainer.jdbcUrl
        val database = Database(mockEnv)
        val dateTime = OffsetDateTime.now(Clock.tickMillis(ZoneOffset.UTC))
        database.insertStatus("3")
        database.updateSendtToAlinn("3", dateTime)
        database.updateSendtToLogg("3", dateTime)
        val status = database.getStatus("3")
        status!!.sykmeldingId shouldBeEqualTo "3"
        status.altinnTimestamp shouldBeEqualTo dateTime
        status.loggTimestamp shouldBeEqualTo dateTime
    }
}
