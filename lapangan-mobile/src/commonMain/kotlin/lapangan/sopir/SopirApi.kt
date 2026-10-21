package lapangan.sopir

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import lapangan.AksiHasil
import lapangan.KlienHttp
import lapangan.pastikanSukses

/**
 * Klien sopir. [baseUrl] memuat awalan `/v1`. Semua aksi tulis membawa UUID klien (idempoten;
 * balasan 200 `duplikat: true` dianggap sukses). [token] mengembalikan JWT akses terkini.
 */
class SopirApi(
    private val client: KlienHttp,
    private val baseUrl: String,
    private val perangkatId: String,
    private val token: () -> String,
) {
    suspend fun tripHariIni(): List<TripDto> =
        client.get("$baseUrl/trip-hari-ini") { tandai() }.pastikanSukses().body<DaftarTrip>().data

    suspend fun tiba(r: TitikTibaRequest): AksiHasil = kirim("$baseUrl/titik/${r.titikId}/tiba", r)

    suspend fun berangkat(r: TitikTibaRequest): AksiHasil = kirim("$baseUrl/titik/${r.titikId}/berangkat", r)

    suspend fun buktiTerima(r: BuktiTerimaRequest): AksiHasil = kirim("$baseUrl/bukti-terima", r)

    suspend fun insiden(r: InsidenRequest): AksiHasil = kirim("$baseUrl/insiden", r)

    private suspend inline fun <reified T> kirim(url: String, badan: T): AksiHasil =
        client.post(url) {
            tandai()
            contentType(ContentType.Application.Json)
            setBody(badan)
        }.pastikanSukses().body()

    private fun io.ktor.client.request.HttpRequestBuilder.tandai() {
        header(HttpHeaders.Authorization, "Bearer ${token()}")
        header("X-Perangkat-Id", perangkatId)
    }
}
