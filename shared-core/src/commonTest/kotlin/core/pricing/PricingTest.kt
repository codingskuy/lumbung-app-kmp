package core.pricing

import core.model.Zona
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FeeCalculatorTest {
    @Test fun contohKanonisZonaDekat() = assertEquals(255_000L, FeeCalculator.hitung(170_000, Zona.DEKAT))

    @Test fun zonaJauh() = assertEquals(425_000L, FeeCalculator.hitung(170_000, Zona.JAUH))

    @Test fun contohPawonRuteUtara() = assertEquals(375_000L, FeeCalculator.hitung(250_000, Zona.DEKAT))

    @Test fun nolGram() = assertEquals(0L, FeeCalculator.hitung(0, Zona.DEKAT))

    @Test fun bukanKelipatanKgDibulatkanKeBawah() = assertEquals(2_251L, FeeCalculator.hitung(1_501, Zona.DEKAT))

    @Test fun beratNegatifDitolak() {
        assertFailsWith<IllegalArgumentException> { FeeCalculator.hitung(-1, Zona.DEKAT) }
    }

    @Test fun tarifPerZona() {
        assertEquals(1_500L, FeeCalculator.tarifPerKg(Zona.DEKAT))
        assertEquals(2_500L, FeeCalculator.tarifPerKg(Zona.JAUH))
    }

    @Test fun zonaDariJarak() {
        assertNull(FeeCalculator.zonaDariJarak(-1))
        assertEquals(Zona.DEKAT, FeeCalculator.zonaDariJarak(0))
        assertEquals(Zona.DEKAT, FeeCalculator.zonaDariJarak(14))
        assertEquals(Zona.JAUH, FeeCalculator.zonaDariJarak(15))
        assertEquals(Zona.JAUH, FeeCalculator.zonaDariJarak(25))
        assertNull(FeeCalculator.zonaDariJarak(26))
    }

    @Test fun benchmark10RibuHitungan() {
        var total = 0L
        repeat(10_000) { total += FeeCalculator.hitung(170_000L + it, Zona.DEKAT) }
        assertEquals(true, total > 0)
    }
}

class PapanHargaTest {
    @Test fun contohKanonis() = assertEquals(31_500L, PapanHarga.hitung(35_000))

    @Test fun contohBrd305() {
        // cabai A 31.500; bawang A 28.000 (median 31.500 -> 28.350 -> 28.000); kangkung A 6.000 (median 7.000 -> 6.300 -> 6.000)
        assertEquals(28_000L, PapanHarga.hitung(31_500))
        assertEquals(6_000L, PapanHarga.hitung(7_000))
    }

    @Test fun dibulatkanKeBawahKelipatan500() {
        assertEquals(0L, PapanHarga.hitung(0))
        assertEquals(0L, PapanHarga.hitung(500)) // 450 -> 0
        assertEquals(500L, PapanHarga.hitung(600)) // 540 -> 500
    }

    @Test fun medianNegatifDitolak() {
        assertFailsWith<IllegalArgumentException> { PapanHarga.hitung(-1) }
    }

    @Test fun medianTigaReferensi() {
        assertEquals(35_000L, PapanHarga.median(listOf(40_000, 30_000, 35_000)))
        assertEquals(31_500L, PapanHarga.hitungDariReferensi(listOf(40_000, 30_000, 35_000)))
    }

    @Test fun referensiHarusTigaTepat() {
        assertFailsWith<IllegalArgumentException> { PapanHarga.median(listOf(1, 2)) }
    }
}
