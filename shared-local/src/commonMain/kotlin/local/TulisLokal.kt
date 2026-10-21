package local

import core.model.Lot
import core.sync.BatasSinkron
import core.sync.PemetaanNomor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import local.db.LumbungDb

/**
 * Pintu tulis lokal-dahulu: simpan ke SQLite lalu antre di outbox dalam SATU transaksi,
 * tanpa menunggu server (AGENTS.md butir 8). Waktu diberikan pemanggil (epoch ms) agar teruji.
 */
public class TulisLokal(private val db: LumbungDb, public val outbox: Outbox = Outbox(db)) {
    private val json = Json

    /** Menyimpan lot (kode boleh `TMP-*`) dan mengantre `lot.buat`. Mengembalikan id outbox. */
    public fun catatLot(lot: Lot, nowMs: Long): String {
        var id = ""
        db.transaction {
            db.lokalQueries.simpanLot(
                lot.jejak.uuid, lot.kode, lot.petaniUuid, lot.tanggal.toString(), lot.komoditas.name,
                lot.beratKotorG, lot.status.name, nowMs, lot.jejak.dibuatOleh,
            )
            // Idempotensi server memakai uuid rekaman.
            id = outbox.tambah("lot.buat", json.encodeToString(lot), nowMs, id = lot.jejak.uuid)
        }
        return id
    }

    public fun kodeLot(uuid: String): String? =
        db.lokalQueries.lotByUuid(uuid).executeAsOneOrNull()?.kode

    /**
     * Catch-up: memetakan nomor sementara ke nomor resmi (maks 200 rekaman per panggilan/request)
     * dan mengganti kode lot terkait.
     */
    public fun terapkanCatchUp(pemetaan: Map<String, String>) {
        require(pemetaan.size <= BatasSinkron.MAKS_REKAMAN_PER_REQUEST) {
            "catch-up maksimal ${BatasSinkron.MAKS_REKAMAN_PER_REQUEST} rekaman per request"
        }
        val valid = PemetaanNomor(pemetaan) // memvalidasi bentuk TMP/resmi
        db.transaction {
            valid.salin().forEach { (tmp, resmi) ->
                db.lokalQueries.petakan(tmp, resmi)
                db.lokalQueries.ubahKodeLot(resmi, tmp)
            }
        }
    }

    public fun nomorTampil(nomor: String): String =
        PemetaanNomor(db.lokalQueries.petaSemua().executeAsList().associate { it.sementara to it.resmi }).tampilkan(nomor)

    /** Mencatat terakhir kali perangkat online (dasar kunci offline >3x24 jam). */
    public fun catatOnline(nowMs: Long) {
        db.lokalQueries.setMeta(KUNCI_ONLINE, nowMs.toString())
    }

    /** Penerbitan surat jalan baru terkunci bila offline >3x24 jam; timbang dan bukti tetap boleh. */
    public fun suratJalanTerkunci(nowMs: Long): Boolean {
        val terakhir = db.lokalQueries.getMeta(KUNCI_ONLINE).executeAsOneOrNull()?.toLongOrNull() ?: return false
        return nowMs - terakhir > BatasSinkron.BATAS_OFFLINE.inWholeMilliseconds
    }

    private companion object {
        const val KUNCI_ONLINE = "terakhir_online_ms"
    }
}
