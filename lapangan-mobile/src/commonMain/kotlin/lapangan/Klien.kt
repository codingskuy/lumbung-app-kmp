package lapangan

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Galat API baku (`Galat` di openapi: `{"error": "..."}`). */
class GalatApi(val status: Int, val pesan: String) : RuntimeException("HTTP $status: $pesan")

@Serializable
internal data class GalatBody(val error: String = "")

/** Hasil aksi idempoten (`AksiHasil`): `duplikat = true` bila UUID sudah pernah diproses. */
@Serializable
data class AksiHasil(val id: String, val duplikat: Boolean)

/** Konfigurasi bersama klien Ktor untuk semua panggilan mobile. */
object KonfigurasiKlien {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    fun terapkan(config: HttpClientConfig<*>) {
        config.install(ContentNegotiation) { json(json) }
        config.expectSuccess = false
    }
}

internal suspend fun HttpResponse.pastikanSukses(): HttpResponse {
    if (!status.isSuccess()) {
        val pesan = runCatching { KonfigurasiKlien.json.decodeFromString(GalatBody.serializer(), bodyAsText()).error }.getOrDefault("")
        throw GalatApi(status.value, pesan)
    }
    return this
}

typealias KlienHttp = HttpClient
