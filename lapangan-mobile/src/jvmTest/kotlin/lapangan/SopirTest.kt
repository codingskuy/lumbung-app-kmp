package lapangan

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import lapangan.sopir.AturanSopir
import lapangan.sopir.KirimOutboxSopir
import lapangan.sopir.SopirApi
import lapangan.sopir.SopirLokal
import lapangan.sopir.TripDto
import lapangan.sopir.Titik
import local.Outbox
import local.db.LumbungDb
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val JSON_H = headersOf(HttpHeaders.ContentType, "application/json")
private val WAKTU = Instant.parse("2026-10-10T01:00:00Z")

private fun outbox() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let {
    LumbungDb.Schema.create(it)
    Outbox(LumbungDb(it))
}

private fun api(engine: MockEngine) =
    SopirApi(HttpClient(engine) { KonfigurasiKlien.terapkan(this) }, "http://x/v1", "HP-1") { "tok" }

class AturanSopirTest {
    private val hari = LocalDate(2026, 10, 10)
    private fun trip(kode: String, tgl: String?, sopir: String?, status: String = "DISUSUN", titik: List<Titik> = emptyList()) =
        TripDto(kode, "UTARA", status, tanggal = tgl, sopir = sopir, titik = titik)

    @Test fun hanyaTripHariItuDanMilikSendiri() {
        val semua = listOf(
            trip("A", "2026-10-10", "sopir-02"),
            trip("B", "2026-10-09", "sopir-02"),
            trip("C", "2026-10-10", "sopir-03"),
            trip("D", "2026-10-10", null),
            trip("E", null, "sopir-02"),
        )
        assertEquals(listOf("A", "D"), AturanSopir.tripSaya(semua, hari, "sopir-02").map { it.kode })
    }

    @Test fun tripAktifDanTitikAktif() {
        assertNull(AturanSopir.tripAktif(listOf(trip("A", null, null))))
        val t = trip("A", null, null, "BERJALAN", listOf(
            Titik("P3", 3), Titik("P1", 1, tibaPada = WAKTU, berangkatPada = WAKTU), Titik("P2", 2, tibaPada = WAKTU),
        ))
        assertEquals("A", AturanSopir.tripAktif(listOf(t))?.kode)
        assertEquals("P2", AturanSopir.titikAktif(t)?.id)
        val belum = trip("B", null, null, titik = listOf(Titik("Q2", 2), Titik("Q1", 1)))
        assertEquals("Q1", AturanSopir.titikAktif(belum)?.id)
        val selesai = trip("C", null, null, titik = listOf(Titik("R1", 1, tibaPada = WAKTU, berangkatPada = WAKTU)))
        assertNull(AturanSopir.titikAktif(selesai))
    }

    @Test fun gpsTiap5MenitHanyaSaatTripAktif() {
        assertFalse(AturanSopir.harusCatatGps(false, null, 0))
        assertTrue(AturanSopir.harusCatatGps(true, null, 0))
        assertFalse(AturanSopir.harusCatatGps(true, 0, 299_999))
        assertTrue(AturanSopir.harusCatatGps(true, 0, 300_000))
    }

    @Test fun validasiFoto() {
        assertTrue(AturanSopir.validasiFoto(listOf(1000, 512_000)).isEmpty())
        assertEquals(1, AturanSopir.validasiFoto(listOf(1000)).size)
        assertTrue(AturanSopir.validasiFoto(listOf(1000, 512_001)).any { "500 KB" in it })
        assertTrue(AturanSopir.validasiFoto(listOf(0, 10)).any { "tidak valid" in it })
    }

    @Test fun batasSinkron2Jam() {
        assertFalse(AturanSopir.melewatiBatasSinkron(0, 7_200_000))
        assertTrue(AturanSopir.melewatiBatasSinkron(0, 7_200_001))
    }
}

class SopirApiTest {
    @Test fun tripHariIniDenganHeader() = runBlocking {
        val engine = MockEngine { req ->
            assertEquals("Bearer tok", req.headers[HttpHeaders.Authorization])
            assertEquals("HP-1", req.headers["X-Perangkat-Id"])
            respond(
                """{"data":[{"kode":"TRIP-UT-041","rute":"UTARA","status":"DISUSUN","tanggal":"2026-10-10","titik":[{"id":"PWN-1","urutan":1,"zona":"DEKAT"}],"baru":1}]}""",
                HttpStatusCode.OK, JSON_H,
            )
        }
        val t = api(engine).tripHariIni().single()
        assertEquals("TRIP-UT-041", t.kode)
        assertEquals("PWN-1", t.titik.single().id)
    }

    @Test fun tibaMengirimBadanSnakeCase() = runBlocking {
        val engine = MockEngine { req ->
            assertEquals(HttpMethod.Post, req.method)
            assertEquals("http://x/v1/titik/PWN-1/tiba", req.url.toString())
            val badan = req.body.toByteArray().decodeToString()
            assertTrue("\"trip_id\":\"TRIP-UT-041\"" in badan && "\"titik_id\":\"PWN-1\"" in badan, badan)
            respond("""{"id":"abc","duplikat":false}""", HttpStatusCode.Created, JSON_H)
        }
        val r = api(engine).tiba(lapangan.sopir.TitikTibaRequest("u", "TRIP-UT-041", "PWN-1", WAKTU))
        assertEquals(false, r.duplikat)
    }

    @Test fun galatDipetakan() = runBlocking {
        val e = assertFailsWith<GalatApi> {
            api(MockEngine { respond("""{"error":"salah"}""", HttpStatusCode.UnprocessableEntity, JSON_H) }).tripHariIni()
        }
        assertEquals(422, e.status)
        assertEquals("salah", e.pesan)
        val e2 = assertFailsWith<GalatApi> {
            api(MockEngine { respond("bukan json", HttpStatusCode.InternalServerError) }).tripHariIni()
        }
        assertEquals("", e2.pesan)
    }
}

class SopirLokalTest {
    @Test fun tulisLokalCepatFifoDenganUuid() {
        val o = outbox()
        val l = SopirLokal(o)
        val ms = measureTimeMillis {
            l.catatTiba("TRIP-UT-041", "PWN-1", WAKTU, 1, -6.9, 107.6)
            l.catatBerangkat("TRIP-UT-041", "PWN-1", WAKTU, 2)
            l.catatInsiden("keterlambatan", "macet", 3)
        }
        assertTrue(ms < 200, "$ms ms")
        assertEquals(listOf("titik.tiba", "titik.berangkat", "insiden"), o.semua().map { it.jenis })
        assertTrue(o.semua().all { core.sync.UuidV4.valid(it.id) })
    }

    @Test fun insidenButuhJenis() {
        assertFailsWith<IllegalArgumentException> { SopirLokal(outbox()).catatInsiden(" ", null, 0) }
    }

    @Test fun buktiTerimaMengantreFotoLaluBukti() {
        val o = outbox()
        val f1 = core.sync.UuidV4.baru(); val f2 = core.sync.UuidV4.baru()
        SopirLokal(o).catatBuktiTerima("T", "P", WAKTU, listOf(f1, f2), listOf(400_000, 300_000), 1, "ok")
        assertEquals(listOf("foto", "foto", "bukti.terima"), o.semua().map { it.jenis })
        assertEquals(700_000L, o.ukuranFotoByte())
        assertTrue(o.semua().last().payload.contains("foto_ids"))
    }

    @Test fun buktiTerimaMenolakFotoTidakValid() {
        val l = SopirLokal(outbox())
        assertFailsWith<IllegalArgumentException> { l.catatBuktiTerima("T", "P", WAKTU, listOf("a"), listOf(10), 0) }
        assertFailsWith<IllegalArgumentException> { l.catatBuktiTerima("T", "P", WAKTU, listOf("a", "b"), listOf(10, 600_000), 0) }
        assertFailsWith<IllegalArgumentException> { l.catatBuktiTerima("T", "P", WAKTU, listOf("a", "b", "c"), listOf(10, 10), 0) }
    }
}

class KirimOutboxSopirTest {
    @Test fun kirimFifoDanHapusYangSukses() = runBlocking {
        val o = outbox(); val l = SopirLokal(o)
        l.catatTiba("T", "P1", WAKTU, 0); l.catatBerangkat("T", "P1", WAKTU, 0); l.catatInsiden("x", null, 0)
        val dipanggil = mutableListOf<String>()
        val a = api(MockEngine { req -> dipanggil += req.url.encodedPath; respond("""{"id":"i","duplikat":true}""", HttpStatusCode.OK, JSON_H) })
        val h = KirimOutboxSopir(o, a).kirim(0, true)
        assertEquals(3, h.terkirim)
        assertEquals(listOf("/v1/titik/P1/tiba", "/v1/titik/P1/berangkat", "/v1/insiden"), dipanggil)
        assertEquals(0, o.jumlah())
    }

    @Test fun gagalMenjadwalUlangDanBerhenti() = runBlocking {
        val o = outbox(); val l = SopirLokal(o)
        l.catatTiba("T", "P1", WAKTU, 0); l.catatInsiden("x", null, 0)
        var panggilan = 0
        val a = api(MockEngine { panggilan++; respond("""{"error":"x"}""", HttpStatusCode.InternalServerError, JSON_H) })
        val h = KirimOutboxSopir(o, a).kirim(0, true)
        assertEquals(1, panggilan)
        assertEquals(0, h.terkirim); assertEquals(1, h.gagal)
        assertEquals(2, o.jumlah())
        assertEquals(1, o.semua().first().percobaan)
        assertEquals(60_000L, o.semua().first().jadwalBerikutMs)
        assertTrue(o.ambilBatch(59_999, true).isEmpty())
    }

    @Test fun buktiTerkirimFotoDitahan() = runBlocking {
        val o = outbox()
        SopirLokal(o).catatBuktiTerima("T", "P", WAKTU, listOf("a", "b"), listOf(10, 10), 0)
        val a = api(MockEngine { respond("""{"id":"i","duplikat":false}""", HttpStatusCode.Created, JSON_H) })
        val h = KirimOutboxSopir(o, a).kirim(0, true)
        assertEquals(1, h.terkirim)
        assertEquals(2, h.ditahan)
        assertEquals(listOf("foto", "foto"), o.semua().map { it.jenis })
    }

    @Test fun jenisTakDikenalDianggapGagal() = runBlocking {
        val o = outbox()
        o.tambah("aneh", "{}", 0)
        val a = api(MockEngine { respond("", HttpStatusCode.OK) })
        assertEquals(1, KirimOutboxSopir(o, a).kirim(0, true).gagal)
    }
}
