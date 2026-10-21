package lapangan.ui

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

/** Empat layar sopir (FSD/10). Kerangka dengan testTag untuk Maestro; isi layar menyusul. */
enum class LayarSopir(val judul: String, val tag: String) {
    TRIP("Trip Hari Ini", "trip-hari-ini"),
    TITIK("Titik Aktif", "titik-aktif"),
    BUKTI("Bukti Terima", "bukti-terima"),
    INSIDEN("Insiden", "insiden"),
}

@Composable
fun SopirApp() {
    var layar by remember { mutableStateOf(LayarSopir.TRIP) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayarSopir.entries.forEach { l ->
                    Button(onClick = { layar = l }, modifier = Modifier.testTag("nav-${l.tag}")) { Text(l.judul) }
                }
            }
            Text(layar.judul, Modifier.testTag("layar-${layar.tag}"))
        }
    }
}
