package core.sync

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/** Konstanta kebijakan sinkron v1.0.0 (FSD/10, ADR-0001, BRD 00 Metrik). */
public object BatasSinkron {
    /** Catch-up maksimal 200 rekaman per request. */
    public const val MAKS_REKAMAN_PER_REQUEST: Int = 200

    /** Antrean sinkron tidak pernah melebihi 200 item. */
    public const val MAKS_ANTREAN: Int = 200

    /** Batch sinkron: 25 keranjang (<100 KB, <30 detik pada 1 Mbps). */
    public const val UKURAN_BATCH_KERANJANG: Int = 25

    /** Foto menunggu WiFi bila antrean melebihi 10 MB. */
    public const val AMBANG_FOTO_WIFI_BYTE: Long = 10L * 1024 * 1024

    /** Foto bukti terima maksimal 500 KB per foto. */
    public const val MAKS_FOTO_BYTE: Long = 500L * 1024

    /** Offline lebih dari 3x24 jam mengunci surat jalan baru. */
    public val BATAS_OFFLINE: Duration = 3.days
}

/** Retry outbox: 1, 5, 15 menit; percobaan berikutnya tetap 15 menit. */
public object KebijakanRetry {
    private val JEDA = listOf(1.minutes, 5.minutes, 15.minutes)

    /** Jeda sebelum percobaan ulang setelah gagal ke-[gagalKe] (mulai 1). */
    public fun jeda(gagalKe: Int): Duration {
        require(gagalKe >= 1) { "gagalKe mulai dari 1" }
        return JEDA[minOf(gagalKe, JEDA.size) - 1]
    }
}

/** Aturan sinkron murni (tanpa I/O). */
public object AturanSinkron {
    /** Foto ditahan sampai WiFi bila antrean foto lebih dari 10 MB dan jaringan bukan WiFi. */
    public fun fotoMenungguWifi(antreanFotoByte: Long, wifi: Boolean): Boolean =
        antreanFotoByte > BatasSinkron.AMBANG_FOTO_WIFI_BYTE && !wifi

    /**
     * Penerbitan surat jalan baru terkunci bila offline lebih dari 3x24 jam.
     * Timbang dan bukti tetap diizinkan (tidak dicek di sini).
     * [terakhirOnline] null = belum pernah online (tidak dianggap terkunci).
     */
    public fun suratJalanTerkunci(terakhirOnline: Instant?, sekarang: Instant): Boolean =
        terakhirOnline != null && sekarang - terakhirOnline > BatasSinkron.BATAS_OFFLINE

    /** Memecah rekaman menjadi potongan maksimal 200 per request catch-up. */
    public fun <T> bagiCatchUp(rekaman: List<T>): List<List<T>> =
        rekaman.chunked(BatasSinkron.MAKS_REKAMAN_PER_REQUEST)

    /** Apakah antrean masih boleh menerima item baru. */
    public fun antreanBolehTerima(ukuranAntrean: Int): Boolean = ukuranAntrean < BatasSinkron.MAKS_ANTREAN
}
