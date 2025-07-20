package io.github.janmalch.simplerssreader.core

import io.github.janmalch.simplerssreader.network.AtomFeed
import io.github.janmalch.simplerssreader.network.RemoteFeed
import io.github.janmalch.simplerssreader.network.RssFeed
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import timber.log.Timber


suspend fun HttpClient.getFeed(url: Url): RemoteFeed {
    val response = get(url)
    if (!response.status.isSuccess()) {
        throw FeedException("Failed to get feed '${url}' due to HTTP ${response.status}.")
    }
    return try {
        when (response.contentType()) {
            ContentType.Application.Atom -> response.body<AtomFeed>()
            ContentType.Application.Rss -> response.body<RssFeed>()
            // seems like most
            else -> {
                val isRss = "<rss" in response.bodyAsText()
                try {
                    if (isRss) response.body<RssFeed>()
                    else response.body<AtomFeed>()
                } catch (e: Exception) {
                    Timber.v(
                        e,
                        "Failed to parse response body of type '%s' for feed '%s' as %s feed. Trying %s format before giving up.",
                        response.contentType(),
                        url,
                        if (isRss) "RSS" else "Atom",
                        if (isRss) "Atom" else "RSS",
                    )
                    if (!isRss) response.body<RssFeed>()
                    else response.body<AtomFeed>()
                }
            }
        }
    } catch (e: Exception) {
        throw FeedException(
            "Failed to parse response body of type '${response.contentType()}' for feed '${url}'.",
            e
        )
    }
}
