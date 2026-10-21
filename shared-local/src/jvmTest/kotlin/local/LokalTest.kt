package local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import core.model.Jejak
import core.model.Komoditas
import core.model.Lot
import core.sync.UuidV4
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import local.db.LumbungDb
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val MENIT = 60_000L
private const val HARI = 24 * 60 * MENIT

private fun baru(): TulisLokal {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    LumbungDb.Schema.create(driver)
    return TulisLokal(LumbungDb(driver))
}

private fun lot(kode: String = "TMP-0a1b2c3d", uuid: String = UuidV4.baru()) = Lot(
    Jejak(uuid, Instant.parse("2026-10-06T01:00:00Z"), 25_200, "OPR-1"),
    kode, "petani-1", LocalDate(2026, 10, 6), Komoditas.CABAI, 120_000,
)

class TulisLokalTest {
    @Test fun tulisLokalCepatDanMengantre() {
        val t = baru()
        val l = lot()
        val ms = measureTimeMillis { t.catatLot(l, 1_000) }
        assertTrue(ms < 200, "tulis lokal $ms ms")
        assertEquals("TMP-0a1b2c3d", t.kodeLot(l.jejak.uuid))
        val item = t.outbox.semua().single()
        assertEquals(l.jejak.uuid, item.id)
        assertEquals("lot.buat", item.jenis)
    }

    @Test fun kodeLotTidakAda() = assertNull(baru().kodeLot("x"))

    @Test fun uuidGandaDitolakDanTransaksiBatal() {
        val t = baru()
        val l = lot()
        t.catatLot(l, 1)
        assertFailsWith<Exception> { t.catatLot(l, 2) }
        assertEquals(1, t.outbox.jumlah())
    }

    @Test fun catchUpMemetakanDanMenggantiKode() {
        val t = baru()
        val l = lot()
        t.catatLot(l, 1)
        t.terapkanCatchUp(mapOf("TMP-0a1b2c3d" to "L-8812"))
        assertEquals("L-8812", t.kodeLot(l.jejak.uuid))
        assertEquals("L-8812", t.nomorTampil("TMP-0a1b2c3d"))
        assertEquals("TMP-ffffffff", t.nomorTampil("TMP-ffffffff"))
    }

    @Test fun catchUpMaks200() {
        val t = baru()
        val besar = (0..200).associate { "TMP-%08x".format(it) to "L-%04d".format(it) }
        assertFailsWith<IllegalArgumentException> { t.terapkanCatchUp(besar) }
        t.terapkanCatchUp(besar.entries.take(200).associate { it.key to it.value })
    }

    @Test fun catchUpNomorTidakValidDitolak() {
        assertFailsWith<IllegalArgumentException> { baru().terapkanCatchUp(mapOf("L-1234" to "L-1235")) }
    }

    @Test fun offlineLebihDari3HariMengunciSuratJalan() {
        val t = baru()
        assertFalse(t.suratJalanTerkunci(10 * HARI)) // belum pernah online
        t.catatOnline(0)
        assertFalse(t.suratJalanTerkunci(3 * HARI))
        assertTrue(t.suratJalanTerkunci(3 * HARI + 1))
        t.catatOnline(3 * HARI + 1)
        assertFalse(t.suratJalanTerkunci(3 * HARI + 2))
    }
}

class OutboxTest {
    private fun outbox() = baru().outbox

    @Test fun urutanFifo() {
        val o = outbox()
        val a = o.tambah("a", "1", 0)
        val b = o.tambah("b", "2", 0)
        val c = o.tambah("c", "3", 0)
        assertEquals(listOf(a, b, c), o.ambilBatch(0, wifi = true).map { it.id })
    }

    @Test fun batchMaks25() {
        val o = outbox()
        repeat(60) { o.tambah("k", "x", 0) }
        assertEquals(25, o.ambilBatch(0, true).size)
        assertEquals(3, o.ambilBatch(0, true, maks = 3).size)
    }

    @Test fun retry1_5_15Menit() {
        val o = outbox()
        val id = o.tambah("k", "x", 0)
        o.tandaiGagal(id, 0)
        assertTrue(o.ambilBatch(MENIT - 1, true).isEmpty())
        assertEquals(1, o.ambilBatch(MENIT, true).size)
        o.tandaiGagal(id, MENIT)
        assertTrue(o.ambilBatch(MENIT + 5 * MENIT - 1, true).isEmpty())
        assertEquals(1, o.ambilBatch(6 * MENIT, true).size)
        o.tandaiGagal(id, 6 * MENIT)
        assertEquals(1, o.ambilBatch(21 * MENIT, true).size)
        o.tandaiGagal(id, 21 * MENIT)
        assertTrue(o.ambilBatch(21 * MENIT + 15 * MENIT - 1, true).isEmpty())
        assertEquals(4, o.semua().single().percobaan)
    }

    @Test fun headOfLineBlocking() {
        val o = outbox()
        val a = o.tambah("a", "1", 0)
        o.tambah("b", "2", 0)
        o.tandaiGagal(a, 0)
        assertTrue(o.ambilBatch(30_000, true).isEmpty())
    }

    @Test fun sukseDihapus() {
        val o = outbox()
        val id = o.tambah("a", "1", 0)
        o.tandaiSukses(id)
        assertEquals(0, o.jumlah())
    }

    @Test fun gagalUntukIdTakDikenalDiabaikan() = outbox().tandaiGagal("tak-ada", 0)

    @Test fun antreanTidakPernahLebihDari200() {
        val o = outbox()
        repeat(200) { o.tambah("k", "x", 0) }
        val e = assertFailsWith<AntreanPenuhException> { o.tambah("k", "x", 0) }
        assertEquals(200, e.ukuran)
        assertEquals(200, o.jumlah())
    }

    @Test fun fotoMenungguWifiBilaAntreanLebihDari10Mb() {
        val o = outbox()
        val foto = o.tambah("foto", "f", 0, ukuranByte = 10L * 1024 * 1024 + 1, foto = true)
        val data = o.tambah("lot", "d", 0)
        assertEquals(listOf(data), o.ambilBatch(0, wifi = false).map { it.id })
        assertEquals(listOf(foto, data), o.ambilBatch(0, wifi = true).map { it.id })
        assertEquals(10L * 1024 * 1024 + 1, o.ukuranFotoByte())
    }

    @Test fun fotoKecilTetapDikirimTanpaWifi() {
        val o = outbox()
        val foto = o.tambah("foto", "f", 0, ukuranByte = 400_000, foto = true)
        assertEquals(listOf(foto), o.ambilBatch(0, wifi = false).map { it.id })
    }
}
