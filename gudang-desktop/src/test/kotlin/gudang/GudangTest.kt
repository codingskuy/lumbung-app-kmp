package gudang

import gudang.api.GudangApi
import gudang.api.HasilMasuk
import gudang.logika.BilahStatus
import gudang.logika.KunciPin
import gudang.logika.LabelKeranjang
import gudang.logika.PenutupShift
import gudang.logika.SesiShift
import gudang.logika.Timbangan
import gudang.logika.TimbangGanda
import core.model.Grade
import core.model.Komoditas
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TimbangTest {
    @Test fun susut() {
        val t = TimbangGanda(120_000, 114_000)
        assertEquals(6_000, t.susutG)
        assertEquals(5, t.susutPersen)
    }

    @Test fun batas() {
        assertFailsWith<IllegalArgumentException> { TimbangGanda(0, 0) }
        assertFailsWith<IllegalArgumentException> { TimbangGanda(150_001, 1) }
        assertFailsWith<IllegalArgumentException> { TimbangGanda(1_000, 1_001) }
        assertFailsWith<IllegalArgumentException> { TimbangGanda(1_000, -1) }
        TimbangGanda(150_000, 0)
    }

    @Test fun timbanganFake() = runBlocking {
        val t = Timbangan { 20_000L }
        assertEquals(20_000L, t.baca())
    }
}

class LabelTest {
    private val tgl = LocalDate(2026, 10, 6)

    @Test fun isiLabelBr105() {
        val baris = LabelKeranjang("PTN-007", tgl, Komoditas.CABAI, Grade.A, 20_000).teks()
        assertTrue(baris.any { "PTN-007" in it })
        assertTrue(baris.any { "2026-10-06" in it })
        assertTrue(baris.any { "cabai" in it })
        assertTrue(baris.any { it.trim().endsWith("A") })
        assertTrue(baris.any { "20 kg" in it })
        assertTrue(baris.all { it.length <= LabelKeranjang.LEBAR })
    }

    @Test fun formatKg() {
        assertEquals("20", LabelKeranjang.formatKg(20_000))
        assertEquals("20,5", LabelKeranjang.formatKg(20_500))
        assertEquals("20,05", LabelKeranjang.formatKg(20_050))
        assertEquals("0,001", LabelKeranjang.formatKg(1))
    }

    @Test fun validasi() {
        assertFailsWith<IllegalArgumentException> { LabelKeranjang("X", tgl, Komoditas.CABAI, Grade.A, 1) }
        assertFailsWith<IllegalArgumentException> { LabelKeranjang("PTN-007", tgl, Komoditas.CABAI, Grade.A, 0) }
        assertFailsWith<IllegalArgumentException> { LabelKeranjang("PTN-007", tgl, Komoditas.CABAI, Grade.C, 1) }
    }
}

class ShiftTest {
    private val t0 = Instant.parse("2026-10-06T01:00:00Z")

    @Test fun tigaSalahMengunci15Menit() {
        val k = KunciPin()
        assertFalse(k.catatSalah(t0))
        assertFalse(k.catatSalah(t0))
        assertTrue(k.catatSalah(t0))
        assertTrue(k.terkunci(t0))
        assertEquals(15, k.sisaKunciMenit(t0))
        assertTrue(k.catatSalah(t0)) // tetap terkunci
        assertTrue(k.terkunci(Instant.parse("2026-10-06T01:14:59Z")))
        assertFalse(k.terkunci(Instant.parse("2026-10-06T01:15:00Z")))
        assertEquals(0, k.sisaKunciMenit(Instant.parse("2026-10-06T01:20:00Z")))
    }

    @Test fun benarMereset() {
        val k = KunciPin()
        k.catatSalah(t0); k.catatSalah(t0)
        k.catatBenar()
        assertFalse(k.catatSalah(t0))
        assertFalse(k.terkunci(t0))
        assertEquals(0, KunciPin().sisaKunciMenit(t0))
    }

    @Test fun tokenShift12Jam() {
        val s = SesiShift("tok", t0)
        assertFalse(s.kedaluwarsa(Instant.parse("2026-10-06T00:59:59Z")))
        assertTrue(s.kedaluwarsa(t0))
    }

    @Test fun tutupShift1400() {
        assertFalse(PenutupShift.waktunyaTutup(LocalTime(13, 59)))
        assertTrue(PenutupShift.waktunyaTutup(LocalTime(14, 0)))
    }

    @Test fun bilah() {
        assertEquals("Online | Antrean: 0", BilahStatus.teks(true, 0))
        assertEquals("Offline | Antrean: 12", BilahStatus.teks(false, 12))
    }
}

class GudangApiTest {
    private fun api(engine: MockEngine) =
        GudangApi(HttpClient(engine) { GudangApi.konfigurasi(this) }, "http://x/v1", "kunci", "PC-GUDANG")

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    @Test fun masukBerhasilKirimHeaderDanBadan() = runBlocking {
        val engine = MockEngine { req ->
            assertEquals(HttpMethod.Post, req.method)
            assertEquals("http://x/v1/gudang/masuk", req.url.toString())
            assertEquals("kunci", req.headers["X-Device-Key"])
            assertEquals("PC-GUDANG", req.headers["X-Perangkat-Id"])
            respond("""{"token":"abc","berlaku_sampai":"2026-10-06T13:00:00Z","tambahan":1}""", HttpStatusCode.OK, json)
        }
        val h = api(engine).masuk("OPR-SORTIR-1", "123456")
        val sesi = assertIs<HasilMasuk.Berhasil>(h).sesi
        assertEquals("abc", sesi.token)
        assertEquals(Instant.parse("2026-10-06T13:00:00Z"), sesi.berlakuSampai)
    }

    @Test fun statusDipetakan() = runBlocking {
        fun hasil(status: HttpStatusCode, badan: String = """{"error":"x"}""") = runBlocking {
            api(MockEngine { respond(badan, status, json) }).masuk("OPR", "123456")
        }
        assertIs<HasilMasuk.PinSalah>(hasil(HttpStatusCode.Unauthorized))
        assertEquals("x", assertIs<HasilMasuk.Ditolak>(hasil(HttpStatusCode.Forbidden)).pesan)
        assertIs<HasilMasuk.Terkunci>(hasil(HttpStatusCode.TooManyRequests))
        val g = assertIs<HasilMasuk.Gagal>(hasil(HttpStatusCode.InternalServerError, "bukan json"))
        assertEquals(500, g.status)
        assertEquals("", g.pesan)
    }

    @Test fun pinHarus6Digit() {
        assertFailsWith<IllegalArgumentException> {
            runBlocking { api(MockEngine { respondOk() }).masuk("OPR", "12345") }
        }
    }

    @Test fun keluar() = runBlocking {
        val engine = MockEngine { req ->
            assertEquals("Bearer tok", req.headers[HttpHeaders.Authorization])
            assertEquals("http://x/v1/gudang/keluar", req.url.toString())
            respond("", HttpStatusCode.NoContent)
        }
        assertTrue(api(engine).keluar("tok"))
        assertFalse(api(MockEngine { respond("", HttpStatusCode.Unauthorized) }).keluar("tok"))
    }
}
