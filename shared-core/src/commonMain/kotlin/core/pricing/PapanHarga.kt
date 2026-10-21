package core.pricing

import core.model.Grade

/**
 * Papan harga mingguan (BRD/30 BR-305): median 3 referensi dikurangi 10%,
 * dibulatkan ke bawah kelipatan Rp500. Pure, tanpa I/O.
 */
public object PapanHarga {
    public const val KELIPATAN_RUPIAH: Long = 500L

    /** `hitung(35_000) = 31_500`. */
    public fun hitung(median: Long): Long {
        require(median >= 0) { "median tidak boleh negatif" }
        // floor(median*0,9) lalu floor ke kelipatan 500 sama dengan floor langsung karena 500 bilangan bulat.
        val setelahBuffer = median * 9L / 10L
        return setelahBuffer / KELIPATAN_RUPIAH * KELIPATAN_RUPIAH
    }

    /** Median dari tepat tiga harga referensi. */
    public fun median(referensi: List<Long>): Long {
        require(referensi.size == 3) { "butuh tepat 3 referensi" }
        return referensi.sorted()[1]
    }

    public fun hitungDariReferensi(referensi: List<Long>): Long = hitung(median(referensi))

    /** Persen harga papan yang diterima petani untuk Grade B (BRD BR-002 beku: A 100%, B 85%, C ditolak). */
    public const val PERSEN_GRADE_B: Long = 85L

    /**
     * Harga papan per kg untuk [grade] dari harga Grade A ([hargaA]): A = 100%, B = 85% dibulatkan ke bawah
     * kelipatan Rp500, C = 0 (ditolak). Contoh: A 31.500 -> B 26.500; A 28.000 -> B 23.500; A 6.000 -> B 5.000.
     */
    public fun hargaGrade(hargaA: Long, grade: Grade): Long {
        require(hargaA >= 0) { "hargaA tidak boleh negatif" }
        return when (grade) {
            Grade.A -> hargaA
            Grade.B -> hargaA * PERSEN_GRADE_B / 100L / KELIPATAN_RUPIAH * KELIPATAN_RUPIAH
            Grade.C -> 0L
        }
    }
}
