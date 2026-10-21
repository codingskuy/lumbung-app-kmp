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

/** Empat layar petani (FSD/10). Kerangka dengan testTag; petani tidak punya kontrol ubah harga. */
enum class LayarPetani(val judul: String, val tag: String) {
    KONTRAK("Kontrak Saya", "kontrak-saya"),
    RENCANA("Rencana Setoran", "rencana-setoran"),
    PEMBAYARAN("Pembayaran Saya", "pembayaran-saya"),
    PAPAN("Papan Harga", "papan-harga"),
}

@Composable
fun PetaniApp() {
    var layar by remember { mutableStateOf(LayarPetani.KONTRAK) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayarPetani.entries.forEach { l ->
                    Button(onClick = { layar = l }, modifier = Modifier.testTag("nav-${l.tag}")) { Text(l.judul) }
                }
            }
            Text(layar.judul, Modifier.testTag("layar-${layar.tag}"))
        }
    }
}
