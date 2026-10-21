package gudang.logika

/** Timbang ganda: KOTOR saat bongkar, BERSIH setelah sortir (BR-101). Berat gram integer. */
public data class TimbangGanda(val kotorG: Long, val bersihG: Long) {
    init {
        require(kotorG in 1..MAKS_TIMBANGAN_G) { "berat kotor di luar kapasitas timbangan 150 kg" }
        require(bersihG in 0..kotorG) { "berat bersih harus 0..kotor" }
    }

    /** Selisih kotor-bersih = susut sortir hari itu (gram). */
    public val susutG: Long get() = kotorG - bersihG

    /** Susut dalam persen (dibulatkan ke bawah), hanya untuk tampilan. */
    public val susutPersen: Int get() = (susutG * 100 / kotorG).toInt()

    public companion object {
        /** Timbangan USB 150 kg. */
        public const val MAKS_TIMBANGAN_G: Long = 150_000L
    }
}

/** Sumber bacaan timbangan (USB/serial); implementasi perangkat keras menyusul, tes memakai fake. */
public fun interface Timbangan {
    public suspend fun baca(): Long
}
