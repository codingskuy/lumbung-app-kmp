package core.sync

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** UUID v4 untuk setiap rekaman yang dibuat di perangkat (FSD/10 butir 1). */
public object UuidV4 {
    @OptIn(ExperimentalUuidApi::class)
    public fun baru(): String = Uuid.random().toString()

    /** Memeriksa bentuk UUID versi 4 (huruf kecil/besar diterima). */
    public fun valid(teks: String): Boolean = POLA.matches(teks)

    private val POLA =
        Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
}
