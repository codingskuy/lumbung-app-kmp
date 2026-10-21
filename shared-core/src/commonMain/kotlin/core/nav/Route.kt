package core.nav

import kotlinx.serialization.Serializable

/**
 * Rute Navigation3 untuk tiga aplikasi (FSD/10 "Urutan Layar"). Sealed + serializable agar
 * back stack dapat disimpan; tidak bergantung pada pustaka navigasi (aturan dependensi shared-core).
 * Modul aplikasi membungkusnya menjadi `NavKey` bila perlu.
 */
@Serializable
public sealed interface Route {
    // Gudang desktop
    @Serializable public data object DasborHariIni : Route
    @Serializable public data object TimbangLot : Route
    @Serializable public data class HasilSortir(val lotUuid: String) : Route
    @Serializable public data class CetakLabel(val lotUuid: String) : Route
    @Serializable public data object SusunTrip : Route
    @Serializable public data class CetakSuratJalan(val tripUuid: String) : Route
    @Serializable public data object ArsipKontrak : Route

    // Sopir
    @Serializable public data object TripHariIni : Route
    @Serializable public data class TitikAktif(val titikUuid: String) : Route
    @Serializable public data class BuktiTerima(val titikUuid: String) : Route
    @Serializable public data class Insiden(val titikUuid: String) : Route

    // Petani
    @Serializable public data object KontrakSaya : Route
    @Serializable public data object RencanaSetoran : Route
    @Serializable public data object PembayaranSaya : Route
    @Serializable public data object PapanHargaPetani : Route
}

/** Peran pengguna aplikasi dan layar awal + urutan layar masing-masing. */
public enum class Peran(public val rutePertama: Route, public val layar: List<Route>) {
    GUDANG(
        Route.DasborHariIni,
        listOf(
            Route.DasborHariIni, Route.TimbangLot, Route.SusunTrip, Route.ArsipKontrak,
        ),
    ),
    SOPIR(Route.TripHariIni, listOf(Route.TripHariIni)),
    PETANI(
        Route.KontrakSaya,
        listOf(Route.KontrakSaya, Route.RencanaSetoran, Route.PembayaranSaya, Route.PapanHargaPetani),
    ),
}
