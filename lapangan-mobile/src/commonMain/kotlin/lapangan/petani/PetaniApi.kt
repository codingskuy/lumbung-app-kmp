package lapangan.petani

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
 * Klien petani. Semua data difilter server-side per id petani (FR-303) dari token; klien tidak
 * mengirim id petani. Papan harga hanya dibaca: tidak ada fungsi untuk mengubah harga.
 */
class PetaniApi(
    private val client: KlienHttp,
    private val baseUrl: String,
    private val perangkatId: String,
    private val token: () -> String,
) {
    /** `POST /auth/masuk` dengan akun petani (mis. `PTN-007`) dan PIN 6 digit. */
    suspend fun masuk(akun: String, pin: String): TokenResponse {
        require(AturanPetani.pinValid(pin)) { "PIN harus 6 digit" }
        return client.post("$baseUrl/auth/masuk") {
            header("X-Perangkat-Id", perangkatId)
            contentType(ContentType.Application.Json)
            setBody(MasukRequest(akun, pin))
        }.pastikanSukses().body()
    }

    suspend fun kontrakSaya(): KontrakSaya = ambil("$baseUrl/kontrak-saya").body()

    suspend fun pembayaranSaya(): List<Pembayaran> = ambil("$baseUrl/pembayaran-saya").body<DaftarPembayaran>().data

    suspend fun papanHarga(): PapanHarga = ambil("$baseUrl/papan-harga").body()

    suspend fun rencanaSetoran(r: RencanaSetoranRequest): AksiHasil =
        client.post("$baseUrl/rencana-setoran") {
            tandai()
            contentType(ContentType.Application.Json)
            setBody(r)
        }.pastikanSukses().body()

    private suspend fun ambil(url: String) = client.get(url) { tandai() }.pastikanSukses()

    private fun io.ktor.client.request.HttpRequestBuilder.tandai() {
        header(HttpHeaders.Authorization, "Bearer ${token()}")
        header("X-Perangkat-Id", perangkatId)
    }
}
