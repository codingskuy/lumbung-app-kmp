package core.sync

/**
 * Nomor sementara `TMP-` + 8 karakter (hex dari UUID) dipakai saat offline untuk lot dan surat jalan,
 * lalu dipetakan ke nomor resmi server `SJ-YYYYMMDD-NNN` / `L-NNNN` (FSD/10 butir 3).
 */
public object NomorRantai {
    private const val AWALAN_TMP = "TMP-"
    private val POLA_TMP = Regex("^TMP-[0-9a-f]{8}$")
    private val POLA_SURAT_JALAN = Regex("^SJ-\\d{8}-\\d{3}$")
    private val POLA_LOT = Regex("^L-\\d{4}$")

    /** Nomor sementara dari UUID (8 hex pertama, huruf kecil). */
    public fun sementaraDari(uuid: String): String {
        require(UuidV4.valid(uuid)) { "bukan UUID v4: $uuid" }
        return AWALAN_TMP + uuid.take(8).lowercase()
    }

    public fun sementara(nomor: String): Boolean = POLA_TMP.matches(nomor)

    public fun suratJalanResmi(nomor: String): Boolean = POLA_SURAT_JALAN.matches(nomor)

    public fun lotResmi(nomor: String): Boolean = POLA_LOT.matches(nomor)

    public fun resmi(nomor: String): Boolean = suratJalanResmi(nomor) || lotResmi(nomor)
}

/**
 * Pemetaan nomor sementara ke nomor resmi hasil catch-up. Murni di memori;
 * penyimpanan permanen dikerjakan modul lokal (SQLDelight, LMB-KMP-003).
 */
public class PemetaanNomor(awal: Map<String, String> = emptyMap()) {
    private val peta = LinkedHashMap<String, String>()

    init {
        awal.forEach { (tmp, resmi) -> petakan(tmp, resmi) }
    }

    /** Memetakan [sementara] ke [resmi]; pemetaan ulang ke nomor berbeda ditolak. */
    public fun petakan(sementara: String, resmi: String) {
        require(NomorRantai.sementara(sementara)) { "bukan nomor sementara: $sementara" }
        require(NomorRantai.resmi(resmi)) { "bukan nomor resmi: $resmi" }
        val lama = peta[sementara]
        require(lama == null || lama == resmi) { "$sementara sudah dipetakan ke $lama" }
        peta[sementara] = resmi
    }

    /** Nomor resmi bila sudah dipetakan; bila tidak, nomor yang diberikan apa adanya. */
    public fun tampilkan(nomor: String): String = peta[nomor] ?: nomor

    public fun sudahDipetakan(sementara: String): Boolean = sementara in peta

    public fun salin(): Map<String, String> = peta.toMap()
}
