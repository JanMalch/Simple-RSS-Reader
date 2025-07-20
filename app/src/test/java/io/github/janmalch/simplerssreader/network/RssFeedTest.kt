package io.github.janmalch.simplerssreader.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.test.runTest
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalXmlUtilApi::class)
class RssFeedTest {

    @Test
    fun `should parse a valid rss feed`() = runTest {
        val input = this::class.java.classLoader!!.getResourceAsStream("example.rss.xml")!!
            .toByteReadChannel()
        val client = createHttpClient(MockEngine { _ ->
            respond(
                content = input,
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = ContentType.Application.Rss.toString(),
                )
            )
        })
        val response = client.get("/feed")
        val feed = response.body<RssFeed>()
        assertEquals(25, feed.channel.items.size)
        val first = feed.channel.items.first()
        assertEquals(Instant.fromEpochSeconds(1752817283), first.pubDate)
    }
}