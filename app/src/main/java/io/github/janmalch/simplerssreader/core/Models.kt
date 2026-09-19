package io.github.janmalch.simplerssreader.core

import io.github.janmalch.simplerssreader.core.FeedItem.Content.Html
import io.github.janmalch.simplerssreader.core.FeedItem.Content.Image
import io.github.janmalch.simplerssreader.core.FeedItem.Content.Link
import io.github.janmalch.simplerssreader.core.FeedItem.Content.Video
import io.github.janmalch.simplerssreader.core.FeedItem.Content.YouTube
import io.github.janmalch.simplerssreader.core.FeedItem.Content.YouTubeShort
import io.github.janmalch.simplerssreader.database.FeedItemEntity
import io.github.janmalch.simplerssreader.network.AtomFeed
import io.github.janmalch.simplerssreader.network.RssFeed
import io.github.janmalch.simplerssreader.settings.FeedSource
import io.ktor.http.Url
import io.ktor.http.fullPath
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class FeedItemId(val id: String) {
    init {
        require(id.isNotBlank()) {
            "ID of a feed item may not be blank, but got '$id'."
        }
    }

    override fun toString(): String = id
}

enum class FeedItemHint(val value: String) {
    Image("image"),
    Video("video"),
    Link("link"),
    YouTube("youtube"),
    YouTubeShort("youtube_short"),
    Html("html"),
    ;

    companion object {
        fun parse(value: String): FeedItemHint =
            FeedItemHint.entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("No FeedItemHint named '$value'.")
    }
}

data class Feed(
    val id: Uuid,
    val title: String,
    val url: Url,
)

fun FeedSource.toModel() = Feed(
    id = Uuid.parse(id),
    title = title,
    url = Url(url)
)

data class FeedItem(
    val id: FeedItemId,
    val title: String,
    val author: String,
    val content: Content,
    val url: Url?,
    val image: Any?,
    val published: Instant?,
    val isRead: Boolean,
) {
    sealed interface Content {
        data class Unknown(val text: String?): Content
        data class Html(val text: String): Content
        data class Image(val url: Url): Content
        data class Video(val url: Url): Content
        data class Link(val url: Url): Content
        data class YouTube(val url: Url): Content
        data class YouTubeShort(val url: Url): Content
    }
}

private fun FeedItemEntity.determineContent(): FeedItem.Content? {
    return when (hint) {
        FeedItemHint.Image -> Image(url = embed ?: return null)
        FeedItemHint.Video -> Video(url = embed ?: return null)
        FeedItemHint.Html -> Html(text = content ?: return null)
        FeedItemHint.Link -> Link(url = embed ?: return null)
        FeedItemHint.YouTube -> YouTube(url = embed ?: return null)
        FeedItemHint.YouTubeShort -> YouTubeShort(url = embed ?: return null)
        else -> null
    }
}

fun FeedItemEntity.toModel() = FeedItem(
    id = id,
    title = title,
    content = determineContent() ?: FeedItem.Content.Unknown(text = content),
    url = url,
    image = image,
    isRead = isRead,
    published = published,
    author = author,
)

private fun Url.isYouTubeShort(): Boolean =
    host == "www.youtube.com" && fullPath.startsWith("/shorts/")

private fun Url.isYouTubeVideo(): Boolean =
    host == "www.youtube.com" && fullPath.startsWith("/watch")

fun AtomFeed.toEntities(source: Uuid): List<FeedItemEntity> = entries.map {
    val isYTVideo = it.link?.href?.isYouTubeVideo() == true
    val isYTShort = it.link?.href?.isYouTubeShort() == true
    FeedItemEntity(
        source = source,
        id = FeedItemId(it.id),
        title = it.title,
        author = it.author?.name?.let { authorName ->
            when {
                isYTShort -> "$authorName - YouTube Shorts"
                isYTVideo -> "$authorName - YouTube"
                else -> authorName
            }
        } ?: title,
        content = it.summary,
        url = it.link?.href,
        published = it.published,
        isRead = false,
        image = it.mediaGroup?.thumbnail?.url
            ?: link?.href?.favicon(),
        hint = when {
            isYTShort -> FeedItemHint.YouTubeShort
            isYTVideo -> FeedItemHint.YouTube
            else -> null
        },
        embed = it.link?.href?.takeIf { isYTVideo || isYTShort },
    )
}

fun RssFeed.toEntities(source: Uuid): List<FeedItemEntity> = channel.items.map {
    FeedItemEntity(
        source = source,
        id = FeedItemId(it.guid),
        title = it.title,
        content = it.description,
        author = it.author ?: channel.title,
        url = it.link,
        published = it.pubDate,
        isRead = false,
        image = channel.image?.url
            ?: channel.link.favicon(),
        hint = when (it.ressitHint) {
            "image" -> FeedItemHint.Image
            "link" -> FeedItemHint.Link
            "video" -> FeedItemHint.Video
            "text" -> FeedItemHint.Html
            "youtube" -> FeedItemHint.YouTube
            else -> null
        },
        embed = it.ressitUrl?.href
    )
}
