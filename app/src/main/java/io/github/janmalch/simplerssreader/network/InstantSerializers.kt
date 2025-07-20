package io.github.janmalch.simplerssreader.network

import kotlinx.datetime.parse
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object InstantSerializers {
    object ISO8601 : KSerializer<Instant> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            serialName = "ISO8601 Timestamp",
            kind = PrimitiveKind.STRING,
        )

        override fun serialize(
            encoder: Encoder,
            value: Instant
        ) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }
    }

    object RFC822 : KSerializer<Instant> {
        // NOTE: technically it should be RFC 822, but let's see if this works too.
        // Otherwise see https://gist.github.com/svenjacobs/ced311bd27ee0cbb135a595fd03e5a1f
        private val dtf = DateTimeComponents.Formats.RFC_1123

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            serialName = "RFC822 Timestamp",
            kind = PrimitiveKind.STRING,
        )

        override fun serialize(
            encoder: Encoder,
            value: Instant
        ) {
            encoder.encodeString(value.format(dtf))
        }

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString(), dtf)
        }
    }
}