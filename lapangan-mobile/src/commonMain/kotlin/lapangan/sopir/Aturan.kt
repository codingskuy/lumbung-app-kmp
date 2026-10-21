package lapangan.sopir

import kotlinx.datetime.LocalDate

/** Aturan murni sopir. Waktu selalu diberikan pemanggil. */
object AturanSopir {
    /** GPS dicatat tiap 5 menit hanya selama trip aktif. */
    const val INTERVAL_GPS_MS: Long = 5 * 60_000L

    /** Bukti terima: 2 foto, maksimal 500 KB per foto (US-003, BR-205). */
    const val JUMLAH_FOTO: Int = 2
    const val MAKS_FOTO_BYTE: Long = 500L * 1024

    /** Bukti terima tersinkron maksimal 2 jam setelah sinyal kembali. */
    const val BATAS_SINKRON_MS: Long = 2 * 60 * 60_000L

    /** Sopir hanya melihat tripnya hari itu (FR-303): tanggal sama dan (bila ada) sopir sama. */
    fun tripSaya(semua: List<TripDto>, hariIni: LocalDate, akun: String): List<TripDto> =
        semua.filter { it.tanggal == hariIni.toString() && (it.sopir == null || it.sopir == akun) }

    /** Trip aktif = status BERJALAN. */
    fun tripAktif(trip: List<TripDto>): TripDto? = trip.firstOrNull { it.status == "BERJALAN" }

    /** Titik aktif = titik pertama (urut) yang sudah tiba tetapi belum berangkat; bila tak ada, yang berikutnya. */
    fun titikAktif(trip: TripDto): Titik? {
        val urut = trip.titik.sortedBy { it.urutan }
        return urut.firstOrNull { it.tibaPada != null && it.berangkatPada == null }
            ?: urut.firstOrNull { it.tibaPada == null }
    }

    /** Catat GPS bila trip aktif dan sudah lewat 5 menit sejak catatan terakhir (atau belum pernah). */
    fun harusCatatGps(tripAktif: Boolean, terakhirMs: Long?, sekarangMs: Long): Boolean =
        tripAktif && (terakhirMs == null || sekarangMs - terakhirMs >= INTERVAL_GPS_MS)

    /** Daftar masalah validasi bukti terima; kosong = valid. */
    fun validasiFoto(ukuranFotoByte: List<Long>): List<String> = buildList {
        if (ukuranFotoByte.size != JUMLAH_FOTO) add("bukti terima butuh tepat $JUMLAH_FOTO foto")
        if (ukuranFotoByte.any { it <= 0 }) add("ukuran foto tidak valid")
        if (ukuranFotoByte.any { it > MAKS_FOTO_BYTE }) add("foto melebihi 500 KB")
    }

    /** True bila bukti sudah menunggu lebih dari 2 jam sejak sinyal kembali. */
    fun melewatiBatasSinkron(sinyalKembaliMs: Long, sekarangMs: Long): Boolean =
        sekarangMs - sinyalKembaliMs > BATAS_SINKRON_MS
}
