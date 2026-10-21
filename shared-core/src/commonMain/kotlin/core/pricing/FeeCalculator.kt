package core.pricing

import core.model.Zona

/**
 * Fee logistik murni per kg (BRD/30 BR-301). Pure, tanpa I/O.
 *
 * Contoh kanonis: `hitung(170_000, Zona.DEKAT) = 255_000`.
 * Klien memakainya hanya untuk pratinjau; angka resmi dari server (AGENTS.md).
 */
public object FeeCalculator {
    public const val TARIF_DEKAT_PER_KG: Long = 1_500L
    public const val TARIF_JAUH_PER_KG: Long = 2_500L

    /** Batas zona dekat (km, eksklusif) dan radius layanan maksimum (km, inklusif). */
    public const val BATAS_DEKAT_KM: Int = 15
    public const val RADIUS_LAYANAN_KM: Int = 25

    public fun tarifPerKg(zona: Zona): Long = when (zona) {
        Zona.DEKAT -> TARIF_DEKAT_PER_KG
        Zona.JAUH -> TARIF_JAUH_PER_KG
    }

    /**
     * Fee dalam rupiah untuk [beratG] gram. Berat bukan kelipatan 1000 g dibulatkan ke bawah
     * ke rupiah terdekat (ASUMSI: dokumen hanya memberi contoh kelipatan kg bulat).
     */
    public fun hitung(beratG: Long, zona: Zona): Long {
        require(beratG >= 0) { "beratG tidak boleh negatif" }
        return beratG * tarifPerKg(zona) / 1_000L
    }

    /** Zona dari jarak (km); `null` bila di luar radius layanan 25 km atau jarak negatif. */
    public fun zonaDariJarak(jarakKm: Int): Zona? = when {
        jarakKm < 0 || jarakKm > RADIUS_LAYANAN_KM -> null
        jarakKm < BATAS_DEKAT_KM -> Zona.DEKAT
        else -> Zona.JAUH
    }
}
