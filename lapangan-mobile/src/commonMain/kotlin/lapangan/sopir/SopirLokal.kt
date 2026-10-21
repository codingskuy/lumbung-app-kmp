package lapangan.sopir

import core.sync.UuidV4
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import lapangan.KonfigurasiKlien
import local.Outbox

/**
 * Tulis lokal-dahulu untuk aksi sopir: setiap aksi langsung masuk outbox FIFO (UUID v4) tanpa
 * menunggu server. Pengiriman dilakukan [KirimOutboxSopir].
 */
class SopirLokal(private val outbox: Outbox) {
    private val json get() = KonfigurasiKlien.json

    fun catatTiba(tripId: String, titikId: String, waktu: Instant, nowMs: Long, lat: Double? = null, lng: Double? = null): String =
        antre(JENIS_TIBA, nowMs) { TitikTibaRequest(it, tripId, titikId, waktu, lat, lng).let { r -> json.encodeToString(r) } }

    fun catatBerangkat(tripId: String, titikId: String, waktu: Instant, nowMs: Long, lat: Double? = null, lng: Double? = null): String =
        antre(JENIS_BERANGKAT, nowMs) { TitikTibaRequest(it, tripId, titikId, waktu, lat, lng).let { r -> json.encodeToString(r) } }

    fun catatInsiden(jenis: String, catatan: String?, nowMs: Long, fotoIds: List<String> = emptyList()): String {
        require(jenis.isNotBlank()) { "jenis insiden wajib diisi" }
        return antre(JENIS_INSIDEN, nowMs) { json.encodeToString(InsidenRequest(it, jenis, catatan, fotoIds)) }
    }

    /**
     * Mencatat bukti terima. Foto sudah tersimpan lokal dengan id [fotoIds] dan ukuran [ukuranFotoByte];
     * validasi 2 foto <=500 KB. Mengantre foto lebih dulu (FIFO) lalu bukti. Mengembalikan id bukti.
     */
    fun catatBuktiTerima(
        tripId: String, titikId: String, waktu: Instant, fotoIds: List<String>, ukuranFotoByte: List<Long>,
        nowMs: Long, catatan: String? = null,
    ): String {
        val masalah = AturanSopir.validasiFoto(ukuranFotoByte)
        require(masalah.isEmpty() && fotoIds.size == ukuranFotoByte.size) { masalah.joinToString("; ").ifEmpty { "foto dan ukuran tidak sama" } }
        fotoIds.zip(ukuranFotoByte).forEach { (id, ukuran) ->
            outbox.tambah(JENIS_FOTO, id, nowMs, ukuranByte = ukuran, foto = true, id = id)
        }
        return antre(JENIS_BUKTI, nowMs) { json.encodeToString(BuktiTerimaRequest(it, tripId, titikId, waktu, fotoIds, null, catatan)) }
    }

    private fun antre(jenis: String, nowMs: Long, payload: (String) -> String): String {
        val id = UuidV4.baru()
        return outbox.tambah(jenis, payload(id), nowMs, id = id)
    }

    companion object {
        const val JENIS_TIBA = "titik.tiba"
        const val JENIS_BERANGKAT = "titik.berangkat"
        const val JENIS_BUKTI = "bukti.terima"
        const val JENIS_INSIDEN = "insiden"
        const val JENIS_FOTO = "foto"
    }
}
