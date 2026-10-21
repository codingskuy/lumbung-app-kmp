package core.sync

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class UuidV4Test {
    @Test fun barusesuaiBentukV4() = repeat(50) { assertTrue(UuidV4.valid(UuidV4.baru())) }
    @Test fun uniks() = assertNotEquals(UuidV4.baru(), UuidV4.baru())
    @Test fun versiLainDitolak() = assertFalse(UuidV4.valid("123e4567-e89b-12d3-a456-426614174000"))
    @Test fun sampahDitolak() = assertFalse(UuidV4.valid("bukan-uuid"))
}

class NomorRantaiTest {
    private val uuid = "AB12CD34-0000-4000-8000-000000000000"

    @Test fun sementaraDariUuid() = assertEquals("TMP-ab12cd34", NomorRantai.sementaraDari(uuid))

    @Test fun sementaraDariUuidBaruValid() = assertTrue(NomorRantai.sementara(NomorRantai.sementaraDari(UuidV4.baru())))

    @Test fun uuidTidakValidDitolak() {
        assertFailsWith<IllegalArgumentException> { NomorRantai.sementaraDari("x") }
    }

    @Test fun polaNomor() {
        assertTrue(NomorRantai.sementara("TMP-0a1b2c3d"))
        assertFalse(NomorRantai.sementara("TMP-0A1B2C3D"))
        assertTrue(NomorRantai.suratJalanResmi("SJ-20261010-014"))
        assertFalse(NomorRantai.suratJalanResmi("SJ-2026-014"))
        assertTrue(NomorRantai.lotResmi("L-8812"))
        assertFalse(NomorRantai.lotResmi("L-88"))
        assertTrue(NomorRantai.resmi("L-8812"))
        assertTrue(NomorRantai.resmi("SJ-20261010-014"))
        assertFalse(NomorRantai.resmi("TMP-0a1b2c3d"))
    }
}

class PemetaanNomorTest {
    @Test fun petakanDanTampilkan() {
        val p = PemetaanNomor()
        p.petakan("TMP-0a1b2c3d", "L-8812")
        assertEquals("L-8812", p.tampilkan("TMP-0a1b2c3d"))
        assertEquals("TMP-ffffffff", p.tampilkan("TMP-ffffffff"))
        assertTrue(p.sudahDipetakan("TMP-0a1b2c3d"))
        assertFalse(p.sudahDipetakan("TMP-ffffffff"))
    }

    @Test fun idempoten() {
        val p = PemetaanNomor()
        p.petakan("TMP-0a1b2c3d", "L-8812")
        p.petakan("TMP-0a1b2c3d", "L-8812")
        assertEquals(mapOf("TMP-0a1b2c3d" to "L-8812"), p.salin())
    }

    @Test fun petakanUlangBedaDitolak() {
        val p = PemetaanNomor(mapOf("TMP-0a1b2c3d" to "L-8812"))
        assertFailsWith<IllegalArgumentException> { p.petakan("TMP-0a1b2c3d", "L-8813") }
    }

    @Test fun masukanTidakValidDitolak() {
        val p = PemetaanNomor()
        assertFailsWith<IllegalArgumentException> { p.petakan("L-8812", "L-8812") }
        assertFailsWith<IllegalArgumentException> { p.petakan("TMP-0a1b2c3d", "TMP-0a1b2c3d") }
    }
}

class KebijakanSinkronTest {
    @Test fun retry1_5_15LaluTetap15() {
        assertEquals(1.minutes, KebijakanRetry.jeda(1))
        assertEquals(5.minutes, KebijakanRetry.jeda(2))
        assertEquals(15.minutes, KebijakanRetry.jeda(3))
        assertEquals(15.minutes, KebijakanRetry.jeda(10))
    }

    @Test fun retryDiBawahSatuDitolak() {
        assertFailsWith<IllegalArgumentException> { KebijakanRetry.jeda(0) }
    }

    @Test fun fotoMenungguWifi() {
        val sepuluhMb = 10L * 1024 * 1024
        assertFalse(AturanSinkron.fotoMenungguWifi(sepuluhMb, wifi = false))
        assertTrue(AturanSinkron.fotoMenungguWifi(sepuluhMb + 1, wifi = false))
        assertFalse(AturanSinkron.fotoMenungguWifi(sepuluhMb + 1, wifi = true))
    }

    @Test fun suratJalanTerkunciSetelah3x24Jam() {
        val t0 = Instant.parse("2026-10-10T00:00:00Z")
        assertFalse(AturanSinkron.suratJalanTerkunci(null, t0))
        assertFalse(AturanSinkron.suratJalanTerkunci(t0, Instant.parse("2026-10-13T00:00:00Z")))
        assertTrue(AturanSinkron.suratJalanTerkunci(t0, Instant.parse("2026-10-13T00:00:01Z")))
    }

    @Test fun catchUpDibagiMaks200() {
        val bagian = AturanSinkron.bagiCatchUp((1..450).toList())
        assertEquals(listOf(200, 200, 50), bagian.map { it.size })
        assertEquals(emptyList(), AturanSinkron.bagiCatchUp(emptyList<Int>()))
    }

    @Test fun antreanMaks200() {
        assertTrue(AturanSinkron.antreanBolehTerima(199))
        assertFalse(AturanSinkron.antreanBolehTerima(200))
    }

    @Test fun konstanta() {
        assertEquals(25, BatasSinkron.UKURAN_BATCH_KERANJANG)
        assertEquals(500L * 1024, BatasSinkron.MAKS_FOTO_BYTE)
    }
}
