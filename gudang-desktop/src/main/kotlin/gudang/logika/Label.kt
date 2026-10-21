package gudang.logika

import core.model.Grade
import core.model.Komoditas
import kotlinx.datetime.LocalDate

/** Label keranjang (BR-105): kode petani, tanggal, komoditas, grade, berat bersih. */
public data class LabelKeranjang(
    val kodePetani: String,
    val tanggal: LocalDate,
    val komoditas: Komoditas,
    val grade: Grade,
    val beratBersihG: Long,
) {
    init {
        require(Regex("^PTN-\\d{3}$").matches(kodePetani)) { "kode petani harus PTN-NNN" }
        require(beratBersihG > 0) { "berat bersih harus positif" }
        require(grade != Grade.C) { "Grade C ditolak, tidak dilabeli" }
    }

    /** Teks polos untuk printer thermal 80 mm (~[LEBAR] karakter per baris). */
    public fun teks(): List<String> = listOf(
        tengah("LUMBUNG"),
        "Petani    : $kodePetani",
        "Tanggal   : $tanggal",
        "Komoditas : ${komoditas.name.lowercase()}",
        "Grade     : ${grade.name}",
        "Berat     : ${formatKg(beratBersihG)} kg",
    )

    public companion object {
        public const val LEBAR: Int = 48

        private fun tengah(s: String) = s.padStart((LEBAR + s.length) / 2).take(LEBAR)

        /** 20000 -> "20", 20500 -> "20,5", 20050 -> "20,05". */
        public fun formatKg(g: Long): String {
            val bulat = g / 1000
            val sisa = (g % 1000).toString().padStart(3, '0').trimEnd('0')
            return if (sisa.isEmpty()) "$bulat" else "$bulat,$sisa"
        }
    }
}
