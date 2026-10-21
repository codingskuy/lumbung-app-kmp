package core.pricing

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
}
