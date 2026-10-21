package core.grading

import core.model.Grade
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AturanGradeTest {
    private fun cabai(p: Int = 80, belang: Int = 0, tangkai: Boolean = true, busuk: Int = 0) =
        AturanGrade.tentukan(Sampel.Cabai(p, belang, tangkai, busuk))

    private fun bawang(d: Int = 30, kering: Boolean = true, kulit: Int = 0, busuk: Int = 0) =
        AturanGrade.tentukan(Sampel.Bawang(d, kering, kulit, busuk))

    private fun kangkung(g: Int = 250, layu: Int = 0, busuk: Int = 0) =
        AturanGrade.tentukan(Sampel.Kangkung(g, layu, busuk))

    @Test fun cabaiA() = assertEquals(Grade.A, cabai())
    @Test fun cabaiPanjang70BukanA() = assertEquals(Grade.B, cabai(p = 70))
    @Test fun cabaiTangkaiPatahBukanA() = assertEquals(Grade.B, cabai(tangkai = false))
    @Test fun cabaiBelang20B() = assertEquals(Grade.B, cabai(belang = 20))
    @Test fun cabaiBelang21PanjangLuarRentangC() = assertEquals(Grade.C, cabai(p = 80, belang = 21))
    @Test fun cabaiPanjang50B() = assertEquals(Grade.B, cabai(p = 50, belang = 30))
    @Test fun cabaiBusuk1BukanA() = assertEquals(Grade.B, cabai(busuk = 1))
    @Test fun cabaiBusuk3C() = assertEquals(Grade.C, cabai(busuk = 3))
    @Test fun cabaiPanjang49BelangBesarC() = assertEquals(Grade.C, cabai(p = 49, belang = 40))

    @Test fun bawangA() = assertEquals(Grade.A, bawang())
    @Test fun bawangDiameter25B() = assertEquals(Grade.B, bawang(d = 25))
    @Test fun bawangBasahBukanA() = assertEquals(Grade.B, bawang(kering = false))
    @Test fun bawangKulit15B() = assertEquals(Grade.B, bawang(kulit = 15))
    @Test fun bawangDiameter15B() = assertEquals(Grade.B, bawang(d = 15, kulit = 50))
    @Test fun bawangKecilKulitBanyakC() = assertEquals(Grade.C, bawang(d = 14, kulit = 16))
    @Test fun bawangBusuk3C() = assertEquals(Grade.C, bawang(busuk = 3))
    @Test fun bawangBusuk2B() = assertEquals(Grade.B, bawang(busuk = 2))

    @Test fun kangkungA() = assertEquals(Grade.A, kangkung())
    @Test fun kangkungLebihBerat() = assertEquals(Grade.A, kangkung(g = 300))
    @Test fun kangkungRingan() = assertEquals(Grade.B, kangkung(g = 200))
    @Test fun kangkungLayu10B() = assertEquals(Grade.B, kangkung(layu = 10))
    @Test fun kangkungLayu11C() = assertEquals(Grade.C, kangkung(layu = 11))
    @Test fun kangkungBusukC() = assertEquals(Grade.C, kangkung(busuk = 1))
    @Test fun kangkungBusukDefaultNol() =
        assertEquals(Grade.A, AturanGrade.tentukan(Sampel.Kangkung(250, 0)))
}

class ValidasiSortirTest {
    @Test fun contohL8812Tepat() {
        val h = ValidasiSortir.validasi(120_000, 100_000, 14_000, 6_000)
        assertTrue(h.valid)
        assertEquals(0L, h.selisihG)
        assertEquals(2_400L, h.toleransiG)
    }

    @Test fun batasAtasDuaPersenDiterima() = assertTrue(ValidasiSortir.validasi(100_000, 102_000, 0, 0).valid)
    @Test fun diAtasDuaPersenDitolak() = assertFalse(ValidasiSortir.validasi(100_000, 102_001, 0, 0).valid)
    @Test fun batasBawahDiterima() = assertTrue(ValidasiSortir.validasi(100_000, 98_000, 0, 0).valid)

    @Test fun diBawahDuaPersenDitolak() {
        val h = ValidasiSortir.validasi(100_000, 97_999, 0, 0)
        assertFalse(h.valid)
        assertEquals(-2_001L, h.selisihG)
    }

    @Test fun kotorNolDitolak() {
        assertFailsWith<IllegalArgumentException> { ValidasiSortir.validasi(0, 0, 0, 0) }
    }

    @Test fun beratNegatifDitolak() {
        assertFailsWith<IllegalArgumentException> { ValidasiSortir.validasi(1000, -1, 0, 0) }
        assertFailsWith<IllegalArgumentException> { ValidasiSortir.validasi(1000, 0, -1, 0) }
        assertFailsWith<IllegalArgumentException> { ValidasiSortir.validasi(1000, 0, 0, -1) }
    }
}
