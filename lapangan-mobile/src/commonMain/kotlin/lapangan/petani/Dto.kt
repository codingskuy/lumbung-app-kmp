package lapangan.petani

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO mengikuti docs/api/openapi.yaml lumbung-backend-service (branch feat/lmb-be-005-trip-sinkron). */
@Serializable
data class MasukRequest(val akun: String, val pin: String)

@Serializable
data class TokenResponse(
    val token: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("berlaku_sampai") val berlakuSampai: String,
    val peran: String,
)

@Serializable
data class Komitmen(
    val komoditas: String,
    @SerialName("komitmen_g") val komitmenG: Long,
    @SerialName("realisasi_g") val realisasiG: Long,
)

@Serializable
data class KontrakSaya(
    val petani: String,
    @SerialName("musim_mulai") val musimMulai: String,
    @SerialName("musim_selesai") val musimSelesai: String,
    val komitmen: List<Komitmen>,
)

/** Status mengikuti spec: MENUNGGU, DIBAYAR, TERLAMBAT. */
@Serializable
data class Pembayaran(
    @SerialName("lot_id") val lotId: String,
    val nominal: Long,
    val status: String,
    @SerialName("jatuh_tempo") val jatuhTempo: String,
    val denda: Long? = null,
)

@Serializable
internal data class DaftarPembayaran(val data: List<Pembayaran>)

@Serializable
data class HargaGrade(val a: Long, val b: Long)

@Serializable
data class FeePapan(val dekat: Long, val jauh: Long)

@Serializable
data class PapanHarga(
    val cabai: HargaGrade,
    val bawang: HargaGrade,
    val kangkung: HargaGrade,
    val fee: FeePapan,
    val berlaku: String,
)

@Serializable
data class RencanaSetoranRequest(
    val uuid: String,
    val komoditas: String,
    @SerialName("estimasi_g") val estimasiG: Long,
    val tanggal: String,
)
