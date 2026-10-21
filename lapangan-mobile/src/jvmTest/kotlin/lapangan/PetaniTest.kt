package lapangan

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import lapangan.petani.AturanPetani
import lapangan.petani.Komitmen
import lapangan.petani.KirimOutboxPetani
import lapangan.petani.PetaniApi
import lapangan.petani.PetaniLokal
import lapangan.petani.Pembayaran
import local.Outbox
import local.db.LumbungDb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val H = headersOf(HttpHeaders.ContentType, "application/json")

private fun outboxP() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).let {
    LumbungDb.Schema.create(it)
    Outbox(LumbungDb(it))
}

private fun apiP(engine: MockEngine) =
    PetaniApi(HttpClient(engine) { KonfigurasiKlien.terapkan(this) }, "http://x/v1", "HP-9") { "tok" }

class AturanPetaniTest {
    @Test fun pinEnamDigit() {
        assertTrue(AturanPetani.pinValid("123456"))
        assertFalse(AturanPetani.pinValid("12345"))
        assertFalse(AturanPetani.pinValid("12345a"))
    }

    @Test fun tokenPerangkat30Hari() {
        val t0 = Instant.parse("2026-10-01T00:00:00Z")
        assertFalse(AturanPetani.tokenPerangkatKedaluwarsa(t0, Instant.parse("2026-10-30T23:59:59Z")))
        assertTrue(AturanPetani.tokenPerangkatKedaluwarsa(t0, Instant.parse("2026-10-31T00:00:00Z")))
    }

    @Test fun maksDuaPerangkat() {
        assertTrue(AturanPetani.bolehDaftarPerangkat(0))
        assertTrue(AturanPetani.bolehDaftarPerangkat(1))
        assertFalse(AturanPetani.bolehDaftarPerangkat(2))
    }

    @Test fun persenRealisasi() {
        assertEquals(50, AturanPetani.persenRealisasi(Komitmen("cabai", 600_000, 300_000)))
        assertEquals(33, AturanPetani.persenRealisasi(Komitmen("cabai", 900_000, 300_000)))
        assertEquals(0, AturanPetani.persenRealisasi(Komitmen("cabai", 0, 10)))
    }

    @Test fun labelStatus() {
        assertTrue("H+1" in AturanPetani.labelStatus("MENUNGGU"))
        assertEquals("Sudah dibayar", AturanPetani.labelStatus("DIBAYAR"))
        assertEquals("Terlambat", AturanPetani.labelStatus("TERLAMBAT"))
        assertEquals("X", AturanPetani.labelStatus("X"))
    }

    @Test fun dendaMaks5Persen() {
        fun p(denda: Long?) = Pembayaran("L-1", 1_000_000, "TERLAMBAT", "2026-10-11", denda)
        assertTrue(AturanPetani.dendaDalamBatas(p(null)))
        assertTrue(AturanPetani.dendaDalamBatas(p(50_000)))
        assertFalse(AturanPetani.dendaDalamBatas(p(50_001)))
        assertFalse(AturanPetani.dendaDalamBatas(p(-1)))
    }

    @Test fun urutanPembayaranTerbaruDulu() {
        val a = Pembayaran("A", 1, "MENUNGGU", "2026-10-10")
        val b = Pembayaran("B", 1, "MENUNGGU", "2026-10-12")
        assertEquals(listOf("B", "A"), AturanPetani.urutkanPembayaran(listOf(a, b)).map { it.lotId })
    }
}

class PetaniApiTest {
    @Test fun masukMengirimAkunDanPin() = runBlocking {
        val e = MockEngine { req ->
            assertEquals("http://x/v1/auth/masuk", req.url.toString())
            assertEquals("HP-9", req.headers["X-Perangkat-Id"])
            val b = req.body.toByteArray().decodeToString()
            assertTrue("\"akun\":\"PTN-007\"" in b && "\"pin\":\"123456\"" in b, b)
            respond("""{"token":"t","refresh_token":"r","berlaku_sampai":"2026-10-21T00:15:00Z","peran":"petani"}""", HttpStatusCode.OK, H)
        }
        val t = apiP(e).masuk("PTN-007", "123456")
        assertEquals("petani", t.peran)
        assertEquals("r", t.refreshToken)
    }

    @Test fun masukPinSalahFormat() {
        assertFailsWith<IllegalArgumentException> { runBlocking { apiP(MockEngine { respond("", HttpStatusCode.OK) }).masuk("PTN-007", "1") } }
    }

    @Test fun masukDitolak401() = runBlocking {
        val e = assertFailsWith<GalatApi> {
            apiP(MockEngine { respond("""{"error":"PIN salah"}""", HttpStatusCode.Unauthorized, H) }).masuk("PTN-007", "123456")
        }
        assertEquals(401, e.status)
    }

    @Test fun kontrakPembayaranPapan() = runBlocking {
        val e = MockEngine { req ->
            assertEquals("Bearer tok", req.headers[HttpHeaders.Authorization])
            when (req.url.encodedPath) {
                "/v1/kontrak-saya" -> respond("""{"petani":"PTN-007","musim_mulai":"2026-10-01","musim_selesai":"2026-12-29","komitmen":[{"komoditas":"cabai","komitmen_g":600000,"realisasi_g":120000}]}""", HttpStatusCode.OK, H)
                "/v1/pembayaran-saya" -> respond("""{"data":[{"lot_id":"L-8812","nominal":3200000,"status":"MENUNGGU","jatuh_tempo":"2026-10-07"}]}""", HttpStatusCode.OK, H)
                "/v1/papan-harga" -> respond("""{"cabai":{"a":31500,"b":26500},"bawang":{"a":28000,"b":23500},"kangkung":{"a":6000,"b":5000},"fee":{"dekat":1500,"jauh":2500},"berlaku":"2026-10-06 s.d. 2026-10-12"}""", HttpStatusCode.OK, H)
                else -> respond("", HttpStatusCode.NotFound)
            }
        }
        val a = apiP(e)
        assertEquals(20, AturanPetani.persenRealisasi(a.kontrakSaya().komitmen.single()))
        assertEquals("L-8812", a.pembayaranSaya().single().lotId)
        assertEquals(31_500L, a.papanHarga().cabai.a)
        assertEquals(1_500L, a.papanHarga().fee.dekat)
    }

    @Test fun galat403() = runBlocking {
        val e = assertFailsWith<GalatApi> { apiP(MockEngine { respond("""{"error":"bukan petani"}""", HttpStatusCode.Forbidden, H) }).kontrakSaya() }
        assertEquals(403, e.status)
    }
}

class PetaniLokalTest {
    @Test fun rencanaTertulisLokalLaluTerkirim() = runBlocking {
        val o = outboxP()
        val id = PetaniLokal(o).catatRencanaSetoran("cabai", 50_000, "2026-10-12", 0)
        assertTrue(core.sync.UuidV4.valid(id))
        var badan = ""
        val a = apiP(MockEngine { req -> badan = req.body.toByteArray().decodeToString(); respond("""{"id":"x","duplikat":false}""", HttpStatusCode.Created, H) })
        val h = KirimOutboxPetani(o, a).kirim(0, true)
        assertEquals(1, h.terkirim)
        assertTrue("\"estimasi_g\":50000" in badan && id in badan, badan)
        assertEquals(0, o.jumlah())
    }

    @Test fun validasiRencana() {
        val l = PetaniLokal(outboxP())
        assertFailsWith<IllegalArgumentException> { l.catatRencanaSetoran("padi", 1, "2026-10-12", 0) }
        assertFailsWith<IllegalArgumentException> { l.catatRencanaSetoran("cabai", 0, "2026-10-12", 0) }
    }

    @Test fun gagalMenjadwalUlang() = runBlocking {
        val o = outboxP()
        PetaniLokal(o).catatRencanaSetoran("bawang", 10, "2026-10-12", 0)
        val a = apiP(MockEngine { respond("""{"error":"x"}""", HttpStatusCode.ServiceUnavailable, H) })
        val h = KirimOutboxPetani(o, a).kirim(0, true)
        assertEquals(1, h.gagal)
        assertEquals(60_000L, o.semua().single().jadwalBerikutMs)
    }

    @Test fun jenisTakDikenalGagal() = runBlocking {
        val o = outboxP()
        o.tambah("aneh", "{}", 0)
        val a = apiP(MockEngine { respond("", HttpStatusCode.OK) })
        assertEquals(1, KirimOutboxPetani(o, a).kirim(0, true).gagal)
    }
}
