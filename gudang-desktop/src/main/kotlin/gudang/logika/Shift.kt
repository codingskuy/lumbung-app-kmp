package gudang.logika

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes

/** Kunci PIN: 3 salah berturut-turut mengunci 15 menit (tiket #4). Murni, waktu dari pemanggil. */
public class KunciPin(private val maksSalah: Int = 3) {
    private var salah = 0
    private var terkunciSampai: Instant? = null

    public fun terkunci(sekarang: Instant): Boolean = terkunciSampai?.let { sekarang < it } ?: false

    public fun sisaKunciMenit(sekarang: Instant): Long =
        terkunciSampai?.let { ((it - sekarang).inWholeSeconds + 59) / 60 }?.coerceAtLeast(0) ?: 0

    /** Mencatat percobaan; mengembalikan true bila kini terkunci. */
    public fun catatSalah(sekarang: Instant): Boolean {
        if (terkunci(sekarang)) return true
        salah++
        if (salah >= maksSalah) {
            terkunciSampai = sekarang + 15.minutes
            salah = 0
        }
        return terkunci(sekarang)
    }

    public fun catatBenar() {
        salah = 0
        terkunciSampai = null
    }
}

/** Token shift 12 jam (produk/50-autentikasi-perangkat). */
public data class SesiShift(val token: String, val berlakuSampai: Instant) {
    public fun kedaluwarsa(sekarang: Instant): Boolean = sekarang >= berlakuSampai
}

public object PenutupShift {
    /** Shift ditutup tiap 14.00 (`POST /v1/gudang/keluar`). */
    public val JAM_TUTUP: LocalTime = LocalTime(14, 0)

    public fun waktunyaTutup(sekarang: LocalTime): Boolean = sekarang >= JAM_TUTUP
}

/** Teks bilah atas: selalu menampilkan Online/Offline dan jumlah antrean. */
public object BilahStatus {
    public fun teks(online: Boolean, antrean: Int): String =
        "${if (online) "Online" else "Offline"} | Antrean: $antrean"
}
