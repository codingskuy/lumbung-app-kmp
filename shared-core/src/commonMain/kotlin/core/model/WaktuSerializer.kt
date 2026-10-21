package core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer ISO 8601 (mis. `2026-10-06T01:00:00Z`) untuk [Instant]. kotlinx-datetime 0.7 memindahkan Instant
 * ke `kotlin.time` dan tidak lagi menyediakan serializer-nya, sedangkan kontrak API memakai teks ISO 8601.
 */
public object InstantIso : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("core.model.InstantIso", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant): Unit = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
