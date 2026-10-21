package core.model

import core.nav.Peran
import core.nav.Route
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModelTest {
    private val json = Json
    private val jejak = Jejak("11111111-1111-4111-8111-111111111111", Instant.parse("2026-10-06T01:00:00Z"), 25_200, "OPR-SORTIR-1")
    private val tgl = LocalDate(2026, 10, 6)

    private inline fun <reified T> bolakBalik(nilai: T) =
        assertEquals(nilai, json.decodeFromString<T>(json.encodeToString(nilai)))

    @Test fun semuaEntitasBolakBalikJson() {
        bolakBalik(jejak)
        bolakBalik(Petani(jejak, "PTN-007", "Slamet", "0812", "Titik A"))
        bolakBalik(Kontrak(jejak, "p", tgl, LocalDate(2027, 1, 4), listOf(Komitmen(Komoditas.CABAI, 600_000))))
        bolakBalik(Lot(jejak, "L-8812", "p", tgl, Komoditas.CABAI, 120_000))
        bolakBalik(Sortir(jejak, "l", 100_000, 14_000, 6_000, "OPR"))
        bolakBalik(Keranjang(jejak, "KRJ-041", "l", Grade.A, 20_000))
        bolakBalik(Trip(jejak, "TRIP-UT-041", RuteTrip.UTARA, "L300", 300_000))
        bolakBalik(Pesanan(jejak, "PSO-20261010-014", "Pawon", Zona.DEKAT, listOf(ItemPesanan(Komoditas.CABAI, Grade.A, 100_000)), 150_000))
        bolakBalik(Pembayaran(jejak, "r", 3_200_000, tgl, StatusPembayaran.DIBAYAR))
    }

    @Test fun statusPembayaranSesuaiKontrak() =
        assertEquals(listOf("MENUNGGU", "DIBAYAR", "TERLAMBAT"), StatusPembayaran.entries.map { it.name })

    @Test fun totalSortir() = assertEquals(120_000L, Sortir(jejak, "l", 100_000, 14_000, 6_000, "OPR").totalG)

    @Test fun enumStatusLengkap() {
        assertEquals(7, StatusPesanan.entries.size)
        assertEquals(4, StatusLot.entries.size)
        assertEquals(3, StatusTrip.entries.size)
        assertEquals(3, Komoditas.entries.size)
        assertEquals(listOf(Grade.A, Grade.B, Grade.C), Grade.entries)
    }

    @Test fun invarian() {
        assertFailsWith<IllegalArgumentException> { Jejak("u", Instant.fromEpochSeconds(0), 0, "x", versi = 0) }
        assertFailsWith<IllegalArgumentException> { Komitmen(Komoditas.CABAI, -1) }
        assertFailsWith<IllegalArgumentException> { Kontrak(jejak, "p", tgl, tgl, emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            Kontrak(jejak, "p", tgl, tgl, listOf(Komitmen(Komoditas.CABAI, 1), Komitmen(Komoditas.CABAI, 2)))
        }
        assertFailsWith<IllegalArgumentException> { Lot(jejak, "L-1", "p", tgl, Komoditas.CABAI, 0) }
        assertFailsWith<IllegalArgumentException> { Sortir(jejak, "l", -1, 0, 0, "x") }
        assertFailsWith<IllegalArgumentException> { Keranjang(jejak, "KRJ-1", "l", Grade.A, 0) }
        assertFailsWith<IllegalArgumentException> { ItemPesanan(Komoditas.CABAI, Grade.A, 0) }
    }

    @Test fun routeBolakBalikJson() {
        val r: Route = Route.HasilSortir("l")
        assertEquals(r, json.decodeFromString<Route>(json.encodeToString(r)))
        val o: Route = Route.DasborHariIni
        assertEquals(o, json.decodeFromString<Route>(json.encodeToString(o)))
    }

    @Test fun peranDanLayarAwal() {
        assertEquals(Route.DasborHariIni, Peran.GUDANG.rutePertama)
        assertEquals(Route.TripHariIni, Peran.SOPIR.rutePertama)
        assertEquals(4, Peran.PETANI.layar.size)
        assertEquals(Route.KontrakSaya, Peran.PETANI.rutePertama)
    }
}
