package core.model

import kotlinx.serialization.Serializable

/** Tiga komoditas pilot v1.0.0 (KONTEKS §4). */
@Serializable
public enum class Komoditas { CABAI, BAWANG, KANGKUNG }

/** Grade hasil sortir (KONTEKS §5). Grade C = ditolak. */
@Serializable
public enum class Grade { A, B, C }

/** Zona fee logistik: Dekat (<15 km) dan Jauh (15-25 km). */
@Serializable
public enum class Zona { DEKAT, JAUH }

/** Status pesanan (FSD/20). */
@Serializable
public enum class StatusPesanan { BARU, DIKONFIRMASI, DIMUAT, DIANTAR, DITERIMA, SHORT, BATAL }

/** Status lot (FSD/20). */
@Serializable
public enum class StatusLot { BARU, DISORTIR, DIMUAT, DITERIMA }

/** Status trip (FSD/20). */
@Serializable
public enum class StatusTrip { DISUSUN, BERJALAN, SELESAI }

/** Rute trip (FSD/20). */
@Serializable
public enum class RuteTrip { UTARA, SELATAN, KOTA }

/**
 * Status pembayaran petani H+1.
 * ASUMSI: nilai enum tidak dibakukan di FSD/20 (hanya "Pembayaran H+1"); diturunkan dari BR-104.
 */
@Serializable
public enum class StatusPembayaran { MENUNGGU, LUNAS, TELAT }
