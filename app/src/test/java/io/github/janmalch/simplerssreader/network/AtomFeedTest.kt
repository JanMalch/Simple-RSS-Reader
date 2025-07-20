package io.github.janmalch.simplerssreader.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalXmlUtilApi::class)
class AtomFeedTest {

    @Test
    fun `should parse a valid atom feed`() = runTest {
        val input = this::class.java.classLoader!!.getResourceAsStream("example.atom.xml")!!
            .toByteReadChannel()
        val client = createHttpClient(MockEngine { _ ->
            respond(
                content = input,
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = ContentType.Application.Atom.toString(),
                )
            )
        })
        val response = client.get("/feed")
        val feed = response.body<AtomFeed>()
        assertEquals(15, feed.entries.size)
        val first = feed.entries.first()
        assertEquals(Instant.fromEpochSeconds(1752778602), first.published)
        assertEquals(Url("https://i1.ytimg.com/vi/49NikeBCzWo/hqdefault.jpg"), first.mediaGroup?.thumbnail?.url)
    }
}