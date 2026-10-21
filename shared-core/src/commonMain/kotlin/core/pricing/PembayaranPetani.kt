package core.pricing

import core.model.Grade

/**
 * Nilai bayar petani H+1 dari berat bersih per grade x harga papan (AGENTS.md "Angka terkunci").
 * Berat gram integer, uang rupiah integer; pecahan rupiah dibulatkan ke bawah. Pure.
 * Klien memakainya hanya untuk pratinjau; angka resmi dari server.
 */
public object PembayaranPetani {
    /** Nilai untuk [beratBersihG] gram Grade [grade] dengan harga papan Grade A [hargaA] per kg. */
    public fun nilai(beratBersihG: Long, hargaA: Long, grade: Grade): Long {
        require(beratBersihG >= 0) { "beratBersihG tidak boleh negatif" }
        return beratBersihG * PapanHarga.hargaGrade(hargaA, grade) / 1_000L
    }
}
