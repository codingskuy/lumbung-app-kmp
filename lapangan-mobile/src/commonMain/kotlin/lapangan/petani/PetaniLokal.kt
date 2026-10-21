package lapangan.petani

import core.sync.UuidV4
import kotlinx.serialization.encodeToString
import lapangan.KonfigurasiKlien
import lapangan.sopir.HasilKirim
import local.Outbox

/** Tulis lokal-dahulu untuk rencana setoran petani (offline-first, UUID v4 idempoten). */
class PetaniLokal(private val outbox: Outbox) {
    fun catatRencanaSetoran(komoditas: String, estimasiG: Long, tanggal: String, nowMs: Long): String {
        require(komoditas in KOMODITAS) { "komoditas harus salah satu dari $KOMODITAS" }
        require(estimasiG > 0) { "estimasi harus positif" }
        val id = UuidV4.baru()
        val payload = KonfigurasiKlien.json.encodeToString(RencanaSetoranRequest(id, komoditas, estimasiG, tanggal))
        return outbox.tambah(JENIS_RENCANA, payload, nowMs, id = id)
    }

    companion object {
        const val JENIS_RENCANA = "rencana.setoran"
        val KOMODITAS = listOf("cabai", "bawang", "kangkung")
    }
}

/** Pengirim FIFO outbox petani; perilaku galat sama dengan pengirim sopir (jadwal ulang, berhenti). */
class KirimOutboxPetani(private val outbox: Outbox, private val api: PetaniApi) {
    suspend fun kirim(nowMs: Long, wifi: Boolean): HasilKirim {
        var terkirim = 0
        val batch = outbox.ambilBatch(nowMs, wifi)
        for (item in batch) {
            val ok = runCatching {
                check(item.jenis == PetaniLokal.JENIS_RENCANA) { "jenis outbox tidak dikenal: ${item.jenis}" }
                api.rencanaSetoran(KonfigurasiKlien.json.decodeFromString(item.payload))
            }.isSuccess
            if (!ok) {
                outbox.tandaiGagal(item.id, nowMs)
                return HasilKirim(terkirim, 1, batch.size - terkirim - 1)
            }
            outbox.tandaiSukses(item.id)
            terkirim++
        }
        return HasilKirim(terkirim, 0, 0)
    }
}
