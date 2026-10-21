package gudang

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import gudang.ui.AppGudang

public fun main(): Unit = application {
    Window(onCloseRequest = ::exitApplication, title = "Lumbung Gudang") { AppGudang() }
}
