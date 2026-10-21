package core.grading

import core.model.Grade

/**
 * Sampel pengukuran satu keranjang untuk pratinjau grade (BRD/10 BR-102).
 * Satuan bulat: milimeter dan persen, agar tanpa galat desimal.
 *
 * Grade resmi tetap ditetapkan petugas/server; ini hanya pratinjau konsisten (ADR-0001 butir 6).
 */
public sealed interface Sampel {
    public val busukPersen: Int

    /** Cabai rawit merah. */
    public data class Cabai(
        val panjangMm: Int,
        val belangPersen: Int,
        val tangkaiUtuh: Boolean,
        override val busukPersen: Int,
    ) : Sampel

    /** Bawang merah. */
    public data class Bawang(
        val diameterMm: Int,
        val kering: Boolean,
        val kulitTerkelupasPersen: Int,
        override val busukPersen: Int,
    ) : Sampel

    /** Kangkung (per ikat). */
    public data class Kangkung(
        val beratIkatG: Int,
        val layuPersen: Int,
        override val busukPersen: Int = 0,
    ) : Sampel
}

/**
 * Aturan ambang grade per komoditas (BR-102). Pure.
 *
 * ASUMSI (dokumen ambigu, dicatat di PR): "Grade B ... panjang 5-7 cm ATAU belang maks 20%"
 * dibaca literal (salah satu cukup, asal busuk <= 2%); kangkung A butuh ikat >= 250 g.
 */
public object AturanGrade {
    public const val BUSUK_MAKS_B_PERSEN: Int = 2

    public fun tentukan(sampel: Sampel): Grade = when (sampel) {
        is Sampel.Cabai -> cabai(sampel)
        is Sampel.Bawang -> bawang(sampel)
        is Sampel.Kangkung -> kangkung(sampel)
    }

    private fun cabai(s: Sampel.Cabai): Grade = when {
        s.panjangMm > 70 && s.belangPersen == 0 && s.tangkaiUtuh && s.busukPersen == 0 -> Grade.A
        s.busukPersen <= BUSUK_MAKS_B_PERSEN && (s.panjangMm in 50..70 || s.belangPersen <= 20) -> Grade.B
        else -> Grade.C
    }

    private fun bawang(s: Sampel.Bawang): Grade = when {
        s.diameterMm > 25 && s.kering && s.kulitTerkelupasPersen == 0 && s.busukPersen == 0 -> Grade.A
        s.busukPersen <= BUSUK_MAKS_B_PERSEN && (s.diameterMm in 15..25 || s.kulitTerkelupasPersen <= 15) -> Grade.B
        else -> Grade.C
    }

    private fun kangkung(s: Sampel.Kangkung): Grade = when {
        s.beratIkatG >= 250 && s.layuPersen == 0 && s.busukPersen == 0 -> Grade.A
        s.layuPersen <= 10 && s.busukPersen == 0 -> Grade.B
        else -> Grade.C
    }
}

/** Hasil validasi total sortir terhadap berat kotor. */
public data class HasilValidasiSortir(
    val valid: Boolean,
    /** total sortir - kotor, gram (negatif = kurang). */
    val selisihG: Long,
    /** Toleransi mutlak 2% dari kotor, gram. */
    val toleransiG: Long,
)

/** Total sortir A+B+C wajib sama dengan berat kotor dalam +-2% (FSD/20, SRS). */
public object ValidasiSortir {
    public const val TOLERANSI_PERSEN: Long = 2L

    public fun validasi(kotorG: Long, aG: Long, bG: Long, cG: Long): HasilValidasiSortir {
        require(kotorG > 0) { "berat kotor harus positif" }
        require(aG >= 0 && bG >= 0 && cG >= 0) { "berat sortir tidak boleh negatif" }
        val selisih = aG + bG + cG - kotorG
        val toleransi = kotorG * TOLERANSI_PERSEN / 100L
        val valid = (if (selisih < 0) -selisih else selisih) <= toleransi
        return HasilValidasiSortir(valid, selisih, toleransi)
    }
}
