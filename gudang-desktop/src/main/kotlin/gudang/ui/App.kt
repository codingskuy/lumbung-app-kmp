package gudang.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import core.nav.Route
import gudang.logika.BilahStatus

/** Tujuh layar gudang (FSD/10). Kerangka: isi layar menyusul. `tag` dipakai Maestro/uji UI. */
internal enum class LayarGudang(val judul: String, val tag: String, val rute: Route) {
    DASBOR("Dasbor Hari Ini", "dasbor", Route.DasborHariIni),
    TIMBANG("Timbang Lot", "timbang-lot", Route.TimbangLot),
    SORTIR("Hasil Sortir", "hasil-sortir", Route.HasilSortir("")),
    LABEL("Cetak Label", "cetak-label", Route.CetakLabel("")),
    TRIP("Susun Trip", "susun-trip", Route.SusunTrip),
    SURAT_JALAN("Cetak Surat Jalan", "cetak-surat-jalan", Route.CetakSuratJalan("")),
    ARSIP("Arsip Kontrak", "arsip-kontrak", Route.ArsipKontrak),
}

@Composable
private fun IsiLayar(layar: LayarGudang) {
    Text(layar.judul, Modifier.testTag("layar-${layar.tag}"))
}

/** Navigation3: back stack berisi [Route] dari shared-core; tiap rute dipetakan ke layarnya lewat entryProvider. */
@Composable
internal fun AppGudang(online: Boolean = false, antrean: Int = 0) {
    val backStack = remember { mutableStateListOf<Route>(Route.DasborHariIni) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Bilah atas selalu tampil.
            Text(BilahStatus.teks(online, antrean), Modifier.testTag("bilah-status"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayarGudang.entries.forEach { l ->
                    Button(
                        onClick = { backStack.clear(); backStack.add(l.rute) },
                        modifier = Modifier.testTag("nav-${l.tag}"),
                    ) { Text(l.judul) }
                }
            }
            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryProvider = entryProvider {
                    entry<Route.DasborHariIni> { IsiLayar(LayarGudang.DASBOR) }
                    entry<Route.TimbangLot> { IsiLayar(LayarGudang.TIMBANG) }
                    entry<Route.HasilSortir> { IsiLayar(LayarGudang.SORTIR) }
                    entry<Route.CetakLabel> { IsiLayar(LayarGudang.LABEL) }
                    entry<Route.SusunTrip> { IsiLayar(LayarGudang.TRIP) }
                    entry<Route.CetakSuratJalan> { IsiLayar(LayarGudang.SURAT_JALAN) }
                    entry<Route.ArsipKontrak> { IsiLayar(LayarGudang.ARSIP) }
                },
            )
        }
    }
}
