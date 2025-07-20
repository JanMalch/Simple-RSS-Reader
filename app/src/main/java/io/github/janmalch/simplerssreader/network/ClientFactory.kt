package io.github.janmalch.simplerssreader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.xml.DefaultXml
import io.ktor.serialization.kotlinx.xml.xml
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlConfig
import java.io.File

private val acceptHeaderValue = arrayOf(
    ContentType.Application.Rss,
    ContentType.Application.Atom,
    ContentType.Application.Xml,
    ContentType.Any,
).joinToString(separator = ", ")

@OptIn(ExperimentalXmlUtilApi::class)
private val xmlFormat = DefaultXml.copy {
    defaultPolicy {
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }
}

fun createHttpClient(engine: HttpClientEngine, cache: File? = null): HttpClient =
    HttpClient(engine) {
        defaultRequest {
            headers {
                append(HttpHeaders.Accept, acceptHeaderValue)
            }
        }
        install(HttpCache) {
            if (cache != null) {
                publicStorage(FileStorage(cache))
                privateStorage(FileStorage(cache))
            }
        }
        install(ContentNegotiation) {
            xml(
                format = xmlFormat,
                contentType = ContentType.Application.Rss,
            )
            xml(
                format = xmlFormat,
                contentType = ContentType.Application.Atom,
            )
            xml(
                format = xmlFormat,
                contentType = ContentType.Application.Xml,
            )
            xml(
                format = xmlFormat,
                contentType = ContentType.Text.Xml,
            )
        }
    }
