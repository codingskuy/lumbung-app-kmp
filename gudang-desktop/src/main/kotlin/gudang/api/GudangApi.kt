package gudang.api

import gudang.logika.SesiShift
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** DTO mengikuti docs/api/openapi.yaml (lumbung-backend-service, branch feat/lmb-be-005-trip-sinkron). */
@Serializable
internal data class GudangMasukRequest(val operator: String, val pin: String)

@Serializable
internal data class TokenShiftResponse(
    val token: String,
    @SerialName("berlaku_sampai") val berlakuSampai: String,
)

@Serializable
internal data class Galat(val error: String)

public sealed interface HasilMasuk {
    public data class Berhasil(val sesi: SesiShift) : HasilMasuk
    public data object PinSalah : HasilMasuk
    public data class Ditolak(val pesan: String) : HasilMasuk
    /** 429: batas laju / terkunci di server. */
    public data object Terkunci : HasilMasuk
    public data class Gagal(val status: Int, val pesan: String) : HasilMasuk
}

/**
 * Klien shift gudang: `POST /gudang/masuk` dan `POST /gudang/keluar`, dengan header `X-Device-Key`
 * dan `X-Perangkat-Id`. [baseUrl] termasuk awalan `/v1`, mis. `http://localhost:8080/v1`.
 */
public class GudangApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val deviceKey: String,
    private val perangkatId: String,
) {
    public suspend fun masuk(operator: String, pin: String): HasilMasuk {
        require(Regex("^\\d{6}$").matches(pin)) { "PIN harus 6 digit" }
        val r = client.post("$baseUrl/gudang/masuk") {
            tandai()
            contentType(ContentType.Application.Json)
            setBody(GudangMasukRequest(operator, pin))
        }
        return when (r.status) {
            HttpStatusCode.OK -> {
                val t = r.body<TokenShiftResponse>()
                HasilMasuk.Berhasil(SesiShift(t.token, Instant.parse(t.berlakuSampai)))
            }
            HttpStatusCode.Unauthorized -> HasilMasuk.PinSalah
            HttpStatusCode.Forbidden -> HasilMasuk.Ditolak(pesan(r))
            HttpStatusCode.TooManyRequests -> HasilMasuk.Terkunci
            else -> HasilMasuk.Gagal(r.status.value, pesan(r))
        }
    }

    /** Menutup shift; true bila 204. */
    public suspend fun keluar(token: String): Boolean {
        val r = client.post("$baseUrl/gudang/keluar") {
            tandai()
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return r.status == HttpStatusCode.NoContent
    }

    private fun io.ktor.client.request.HttpRequestBuilder.tandai() {
        header("X-Device-Key", deviceKey)
        header("X-Perangkat-Id", perangkatId)
    }

    private suspend fun pesan(r: io.ktor.client.statement.HttpResponse): String =
        runCatching { r.body<Galat>().error }.getOrDefault("")

    public companion object {
        public fun konfigurasi(config: io.ktor.client.HttpClientConfig<*>) {
            config.install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            config.expectSuccess = false
        }
    }
}
