package lapangan.sopir

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO mengikuti docs/api/openapi.yaml lumbung-backend-service (branch feat/lmb-be-005-trip-sinkron). */
@Serializable
data class Titik(
    val id: String,
    val urutan: Int,
    val zona: String? = null,
    @SerialName("tiba_pada") val tibaPada: Instant? = null,
    @SerialName("berangkat_pada") val berangkatPada: Instant? = null,
)

@Serializable
data class TripDto(
    val kode: String,
    val rute: String,
    val status: String,
    val kendaraan: String? = null,
    @SerialName("kas_jalan") val kasJalan: Long? = null,
    val tanggal: String? = null,
    val sopir: String? = null,
    val titik: List<Titik> = emptyList(),
)

@Serializable
internal data class DaftarTrip(val data: List<TripDto>)

@Serializable
data class TitikTibaRequest(
    val uuid: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("titik_id") val titikId: String,
    val waktu: Instant,
    val lat: Double? = null,
    val lng: Double? = null,
)

@Serializable
data class BuktiTerimaRequest(
    val uuid: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("titik_id") val titikId: String,
    val waktu: Instant,
    @SerialName("foto_ids") val fotoIds: List<String>,
    @SerialName("tanda_tangan") val tandaTangan: String? = null,
    val catatan: String? = null,
)

@Serializable
data class InsidenRequest(
    val uuid: String,
    val jenis: String,
    val catatan: String? = null,
    @SerialName("foto_ids") val fotoIds: List<String> = emptyList(),
)
