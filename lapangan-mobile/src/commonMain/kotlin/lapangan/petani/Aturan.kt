package lapangan.petani

import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

/** Aturan murni petani. Angka resmi (denda, nominal) selalu dari server; klien hanya menampilkan. */
object AturanPetani {
    /** Token perangkat 30 hari, maksimal 2 perangkat per petani, PIN 6 digit (produk/50). */
    val MASA_TOKEN_PERANGKAT = 30.days
    const val MAKS_PERANGKAT: Int = 2
    private val POLA_PIN = Regex("^\\d{6}$")

    fun pinValid(pin: String): Boolean = POLA_PIN.matches(pin)

    fun tokenPerangkatKedaluwarsa(dibuat: Instant, sekarang: Instant): Boolean =
        sekarang - dibuat >= MASA_TOKEN_PERANGKAT

    /** Perangkat baru boleh didaftarkan bila yang terdaftar masih kurang dari 2. */
    fun bolehDaftarPerangkat(jumlahTerdaftar: Int): Boolean = jumlahTerdaftar < MAKS_PERANGKAT

    /** Persen realisasi terhadap komitmen (dibulatkan ke bawah), hanya untuk tampilan; komitmen 0 -> 0. */
    fun persenRealisasi(k: Komitmen): Int =
        if (k.komitmenG <= 0) 0 else (k.realisasiG * 100 / k.komitmenG).toInt()

    /** Teks status pembayaran H+1 untuk layar. */
    fun labelStatus(status: String): String = when (status) {
        "MENUNGGU" -> "Menunggu pembayaran H+1"
        "DIBAYAR" -> "Sudah dibayar"
        "TERLAMBAT" -> "Terlambat"
        else -> status
    }

    /**
     * Pemeriksaan kewajaran denda dari server: 0,5%/hari maksimal 5% (BR-104), yaitu denda <= 5% nominal.
     * Hanya untuk menandai data aneh; angka tetap ditampilkan apa adanya.
     */
    fun dendaDalamBatas(p: Pembayaran): Boolean {
        val denda = p.denda ?: return true
        return denda in 0..(p.nominal * 5 / 100)
    }

    /** Mengurutkan pembayaran dengan jatuh tempo terbaru lebih dulu (format ISO tanggal). */
    fun urutkanPembayaran(daftar: List<Pembayaran>): List<Pembayaran> = daftar.sortedByDescending { it.jatuhTempo }
}
