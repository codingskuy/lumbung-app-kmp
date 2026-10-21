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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
internal fun AppGudang(online: Boolean = false, antrean: Int = 0) {
    var layar by remember { mutableStateOf(LayarGudang.DASBOR) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Bilah atas selalu tampil.
            Text(BilahStatus.teks(online, antrean), Modifier.testTag("bilah-status"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayarGudang.entries.forEach { l ->
                    Button(onClick = { layar = l }, modifier = Modifier.testTag("nav-${l.tag}")) { Text(l.judul) }
                }
            }
            Text(layar.judul, Modifier.testTag("layar-${layar.tag}"))
        }
    }
}
