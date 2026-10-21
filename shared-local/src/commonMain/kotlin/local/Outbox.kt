package local

import core.sync.AturanSinkron
import core.sync.BatasSinkron
import core.sync.KebijakanRetry
import core.sync.UuidV4
import local.db.LumbungDb

/** Satu rekaman antrean yang menunggu dikirim ke server. */
public data class ItemOutbox(
    val seq: Long,
    /** UUID v4; dipakai sebagai kunci idempotensi di server. */
    val id: String,
    val jenis: String,
    val payload: String,
    val ukuranByte: Long,
    val adalahFoto: Boolean,
    val dibuatPadaMs: Long,
    val percobaan: Int,
    val jadwalBerikutMs: Long,
)

public class AntreanPenuhException(public val ukuran: Int) :
    IllegalStateException("Antrean outbox penuh ($ukuran item, maks ${BatasSinkron.MAKS_ANTREAN})")

/**
 * Outbox FIFO di SQLDelight (FSD/10 butir 2).
 *
 * ASUMSI: FIFO ketat per item (head-of-line): batch berhenti pada item pertama yang belum jatuh tempo retry,
 * agar urutan sebab-akibat (lot sebelum sortir) terjaga. Satu-satunya pengecualian: foto ditahan
 * (dilewati) saat antrean foto >10 MB dan bukan WiFi.
 */
public class Outbox(private val db: LumbungDb) {
    private val q get() = db.outboxQueries

    /** Menambah item; menolak bila antrean sudah [BatasSinkron.MAKS_ANTREAN]. Mengembalikan id item. */
    public fun tambah(
        jenis: String,
        payload: String,
        nowMs: Long,
        ukuranByte: Long = payload.length.toLong(),
        foto: Boolean = false,
        id: String = UuidV4.baru(),
    ): String {
        val ukuran = jumlah()
        if (!AturanSinkron.antreanBolehTerima(ukuran)) throw AntreanPenuhException(ukuran)
        q.tambah(id, jenis, payload, ukuranByte, if (foto) 1 else 0, nowMs, nowMs)
        return id
    }

    public fun jumlah(): Int = q.jumlah().executeAsOne().toInt()

    public fun ukuranFotoByte(): Long = q.ukuranFoto().executeAsOne()

    public fun semua(): List<ItemOutbox> = q.semua().executeAsList().map { it.keItem() }

    /** Batch berikutnya (maks [maks], default 25) yang siap dikirim pada [nowMs]. */
    public fun ambilBatch(nowMs: Long, wifi: Boolean, maks: Int = BatasSinkron.UKURAN_BATCH_KERANJANG): List<ItemOutbox> {
        val tahanFoto = AturanSinkron.fotoMenungguWifi(ukuranFotoByte(), wifi)
        val hasil = ArrayList<ItemOutbox>()
        for (item in semua()) {
            if (hasil.size >= maks) break
            if (item.adalahFoto && tahanFoto) continue
            if (item.jadwalBerikutMs > nowMs) break
            hasil += item
        }
        return hasil
    }

    public fun tandaiSukses(id: String) {
        q.hapus(id)
    }

    /** Menjadwalkan ulang setelah gagal: jeda 1, 5, 15 menit (lalu tetap 15). */
    public fun tandaiGagal(id: String, nowMs: Long) {
        val item = q.ambil(id).executeAsOneOrNull() ?: return
        val jeda = KebijakanRetry.jeda(item.percobaan.toInt() + 1)
        q.gagal(nowMs + jeda.inWholeMilliseconds, id)
    }

    private fun local.db.Outbox.keItem() = ItemOutbox(
        seq, id, jenis, payload, ukuran_byte, adalah_foto != 0L, dibuat_pada_ms, percobaan.toInt(), jadwal_berikut_ms,
    )
}
