package lapangan.sopir

import lapangan.KonfigurasiKlien
import local.Outbox

/** Ringkasan satu putaran pengiriman. */
data class HasilKirim(val terkirim: Int, val gagal: Int, val ditahan: Int)

/**
 * Mengirim batch outbox ke server secara FIFO. Sukses/duplikat -> hapus; galat apa pun -> jadwal ulang
 * 1/5/15 menit dan putaran berhenti agar urutan terjaga.
 *
 * BELUM: unggah biner foto (`POST /unggah-foto`) dan dead-letter untuk galat permanen (422/409);
 * item foto dilewati dan tetap di antrean.
 */
class KirimOutboxSopir(private val outbox: Outbox, private val api: SopirApi) {
    suspend fun kirim(nowMs: Long, wifi: Boolean): HasilKirim {
        var terkirim = 0
        val batch = outbox.ambilBatch(nowMs, wifi)
        for (item in batch) {
            if (item.jenis == SopirLokal.JENIS_FOTO) continue // unggah foto belum diimplementasi
            val sukses = runCatching { kirimSatu(item.jenis, item.payload) }.isSuccess
            if (!sukses) {
                outbox.tandaiGagal(item.id, nowMs)
                return HasilKirim(terkirim, gagal = 1, ditahan = batch.size - terkirim - 1)
            }
            outbox.tandaiSukses(item.id)
            terkirim++
        }
        return HasilKirim(terkirim, 0, batch.count { it.jenis == SopirLokal.JENIS_FOTO })
    }

    private suspend fun kirimSatu(jenis: String, payload: String) {
        val json = KonfigurasiKlien.json
        when (jenis) {
            SopirLokal.JENIS_TIBA -> api.tiba(json.decodeFromString(payload))
            SopirLokal.JENIS_BERANGKAT -> api.berangkat(json.decodeFromString(payload))
            SopirLokal.JENIS_BUKTI -> api.buktiTerima(json.decodeFromString(payload))
            SopirLokal.JENIS_INSIDEN -> api.insiden(json.decodeFromString(payload))
            else -> error("jenis outbox tidak dikenal: $jenis")
        }
    }
}
