package io.github.janmalch.simplerssreader.network

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.time.Instant

// https://github.com/pdvrieze/xmlutil/issues/229#issuecomment-2243527380
private const val XmlnsYt = "http://www.youtube.com/xml/schemas/2015"
private const val XmlnsMedia = "http://search.yahoo.com/mrss/"
private const val XmlnsAtom = "http://www.w3.org/2005/Atom"

@Serializable
@SerialName("feed") //, "", "")
data class AtomFeed(
    @XmlElement
    @SerialName("title")
    val title: String,

    @XmlElement
    @SerialName("link")
    val link: Link? = null,

    @XmlElement
    @SerialName("published")
    @Serializable(with = InstantSerializers.ISO8601::class)
    val published: Instant? = null,

    @XmlElement
    @SerialName("author")
    val author: Author? = null,

    @XmlElement
    @SerialName("id")
    val id: String,

    @XmlElement
    @SerialName("entry")
    val entries: List<Entry> = emptyList()
) : RemoteFeed {

    @Serializable
    @SerialName("author")
    data class Author(
        @XmlElement
        @SerialName("name")
        val name: String? = null,

        @XmlElement
        @SerialName("uri")
        val uri: Url? = null
    )

    @Serializable
    @SerialName("link")
    data class Link(
        @SerialName("href")
        val href: Url
    )

    @Serializable
    @SerialName("entry")
    data class Entry(
        @XmlElement
        @SerialName("title")
        val title: String,

        @XmlElement
        @SerialName("link")
        val link: Link? = null,

        @XmlElement
        @SerialName("id")
        val id: String,

        @XmlElement
        @SerialName("updated")
        @Serializable(with = InstantSerializers.ISO8601::class)
        val updated: Instant? = null,

        @XmlElement
        @SerialName("published")
        @Serializable(with = InstantSerializers.ISO8601::class)
        val published: Instant? = null,

        @XmlElement
        @SerialName("summary")
        val summary: String? = null,

        @XmlElement
        @SerialName("author")
        val author: Author? = null,

        @XmlElement
        @XmlSerialName("group", XmlnsMedia, "media")
        val mediaGroup: MediaGroup? = null,
    )

    @Serializable
    @XmlSerialName("group", XmlnsMedia, "media")
    data class MediaGroup(
        @XmlElement
        @XmlSerialName("thumbnail", XmlnsMedia, "media")
        val thumbnail: Thumbnail? = null,
    ) {
        @Serializable
        @XmlSerialName("thumbnail", XmlnsMedia, "media")
        data class Thumbnail(
            @SerialName("url")
            val url: Url? = null,
        )
    }

}
