package io.github.janmalch.simplerssreader.network

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.time.Instant

private const val XmlnsRessit = "https://github.com/JanMalch/ressit"

@Serializable
@SerialName("rss")
data class RssFeed(
    @XmlElement
    @SerialName("channel")
    val channel: Channel,
    @SerialName("version")
    val version: String,
): RemoteFeed {

    @Serializable
    @SerialName("channel")
    data class Channel(
        @XmlElement
        @SerialName("title")
        val title: String,

        @XmlElement
        @SerialName("description")
        val description: String,

        @XmlElement
        @SerialName("link")
        val link: Url,

        @XmlElement
        @SerialName("image")
        val image: Image? = null,

        @XmlElement
        @SerialName("pubDate")
        @Serializable(with = InstantSerializers.RFC822::class)
        val pubDate: Instant? = null,

        @XmlElement
        @SerialName("lastBuildDate")
        @Serializable(with = InstantSerializers.RFC822::class)
        val lastBuildDate: Instant? = null,

        @XmlElement
        @SerialName("generator")
        val generator: String? = null,

        @SerialName("item")
        val items: List<Item> = emptyList()
    )

    @Serializable
    @SerialName("image")
    data class Image(
        @XmlElement @SerialName("url") val url: Url? = null,
        @XmlElement @SerialName("title") val title: String? = null,
        @XmlElement @SerialName("link") val link: Url? = null,
    )

    @Serializable
    @SerialName("item")
    data class Item(
        @XmlElement
        @SerialName("guid")
        val guid: String,

        @XmlElement
        @SerialName("title")
        val title: String,

        @XmlElement
        @SerialName("link")
        val link: Url? = null,

        @XmlElement
        @SerialName("description")
        val description: String? = null,

        @XmlElement
        @SerialName("author")
        val author: String? = null,

        @XmlElement
        @SerialName("pubDate")
        @Serializable(with = InstantSerializers.RFC822::class)
        val pubDate: Instant? = null,

        /**
         * Usually `text`, `link`, `image`, or `video`.
         */
        @XmlElement
        @XmlSerialName("hint", XmlnsRessit, "ressit")
        val ressitHint: String? = null,
        @XmlElement
        @XmlSerialName("url", XmlnsRessit, "ressit")
        val ressitUrl: RessitUrl? = null,
    ) {

        @Serializable
        @XmlSerialName("url", XmlnsRessit, "ressit")
        data class RessitUrl(
            @XmlSerialName("href", "", "")
            val href: Url? = null,
        )

    }
}