package core.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Metadata wajib tiap rekaman yang dibuat di perangkat (produk/20-model-data butir 4):
 * `uuid` v4, `dibuat_pada` (instan + offset zona), `dibuat_oleh`, `versi` (mulai 1, naik tiap sinkron).
 */
@Serializable
public data class Jejak(
    val uuid: String,
    val dibuatPada: Instant,
    /** Offset zona waktu lokal saat rekaman dibuat, dalam detik dari UTC (WIB = 25200). */
    val offsetDetik: Int,
    val dibuatOleh: String,
    val versi: Int = 1,
) {
    init {
        require(versi >= 1) { "versi mulai dari 1" }
    }
}

/** Semua berat dalam gram (Long), uang dalam rupiah (Long). Tidak ada desimal. */
@Serializable
public data class Petani(
    val jejak: Jejak,
    /** Format `PTN-NNN`. */
    val kode: String,
    val nama: String,
    val kontak: String,
    val titikKumpul: String,
)

/** Komitmen satu komoditas dalam kontrak musim 90 hari. */
@Serializable
public data class Komitmen(val komoditas: Komoditas, val beratG: Long) {
    init {
        require(beratG >= 0) { "beratG tidak boleh negatif" }
    }
}

@Serializable
public data class Kontrak(
    val jejak: Jejak,
    val petaniUuid: String,
    val mulai: LocalDate,
    val selesai: LocalDate,
    /** 1 sampai 3 komoditas (BR-103). */
    val komitmen: List<Komitmen>,
) {
    init {
        require(komitmen.size in 1..3) { "petani memilih 1 sampai 3 komoditas" }
        require(komitmen.map { it.komoditas }.toSet().size == komitmen.size) { "komoditas ganda" }
    }
}

@Serializable
public data class Lot(
    val jejak: Jejak,
    /** Format `L-NNNN` (resmi) atau `TMP-xxxxxxxx` (sementara saat offline). */
    val kode: String,
    val petaniUuid: String,
    val tanggal: LocalDate,
    val komoditas: Komoditas,
    val beratKotorG: Long,
    val status: StatusLot = StatusLot.BARU,
) {
    init {
        require(beratKotorG > 0) { "berat kotor harus positif" }
    }
}

@Serializable
public data class Sortir(
    val jejak: Jejak,
    val lotUuid: String,
    val beratAG: Long,
    val beratBG: Long,
    val beratCG: Long,
    val petugas: String,
) {
    init {
        require(beratAG >= 0 && beratBG >= 0 && beratCG >= 0) { "berat sortir tidak boleh negatif" }
    }

    public val totalG: Long get() = beratAG + beratBG + beratCG
}

@Serializable
public data class Keranjang(
    val jejak: Jejak,
    /** Format `KRJ-NNN`. */
    val kode: String,
    val lotUuid: String,
    val grade: Grade,
    val beratBersihG: Long,
) {
    init {
        require(beratBersihG > 0) { "berat bersih harus positif" }
    }
}

@Serializable
public data class Trip(
    val jejak: Jejak,
    /** Format `TRIP-{RUTE}-NNN`, contoh `TRIP-UT-041`. */
    val kode: String,
    val rute: RuteTrip,
    val kendaraan: String,
    /** Kas jalan, rupiah. */
    val kasJalan: Long,
    val status: StatusTrip = StatusTrip.DISUSUN,
)

@Serializable
public data class ItemPesanan(val komoditas: Komoditas, val grade: Grade, val beratG: Long) {
    init {
        require(beratG > 0) { "berat item harus positif" }
    }
}

@Serializable
public data class Pesanan(
    val jejak: Jejak,
    /** Format `PSO-YYYYMMDD-NNN`. */
    val kode: String,
    val pembeli: String,
    val zona: Zona,
    val item: List<ItemPesanan>,
    /** Nominal fee (rupiah). Angka resmi dari server; klien hanya menampilkan. */
    val feeRupiah: Long,
    val status: StatusPesanan = StatusPesanan.BARU,
)

@Serializable
public data class Pembayaran(
    val jejak: Jejak,
    /** UUID pesanan atau kontrak yang dibayar. */
    val rujukanUuid: String,
    val nominalRupiah: Long,
    /** Jatuh tempo H+1. */
    val jatuhTempo: LocalDate,
    val status: StatusPembayaran = StatusPembayaran.MENUNGGU,
)
