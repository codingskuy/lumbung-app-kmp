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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import core.nav.Route

/** Empat layar sopir (FSD/10). Kerangka dengan testTag untuk Maestro; isi layar menyusul. */
enum class LayarSopir(val judul: String, val tag: String, val rute: Route) {
    TRIP("Trip Hari Ini", "trip-hari-ini", Route.TripHariIni),
    TITIK("Titik Aktif", "titik-aktif", Route.TitikAktif("")),
    BUKTI("Bukti Terima", "bukti-terima", Route.BuktiTerima("")),
    INSIDEN("Insiden", "insiden", Route.Insiden("")),
}

@Composable
private fun IsiSopir(layar: LayarSopir) {
    Text(layar.judul, Modifier.testTag("layar-${layar.tag}"))
}

/** Navigation3: back stack berisi [Route] dari shared-core. */
@Composable
fun SopirApp() {
    val backStack = remember { mutableStateListOf<Route>(Route.TripHariIni) }
    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayarSopir.entries.forEach { l ->
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
                    entry<Route.TripHariIni> { IsiSopir(LayarSopir.TRIP) }
                    entry<Route.TitikAktif> { IsiSopir(LayarSopir.TITIK) }
                    entry<Route.BuktiTerima> { IsiSopir(LayarSopir.BUKTI) }
                    entry<Route.Insiden> { IsiSopir(LayarSopir.INSIDEN) }
                },
            )
        }
    }
}
