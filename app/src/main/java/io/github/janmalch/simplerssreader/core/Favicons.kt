package io.github.janmalch.simplerssreader.core

import io.ktor.http.URLBuilder
import io.ktor.http.Url


private val lookup = mapOf(
    "www.lebensmittelwarnung.de" to "https://www.lebensmittelwarnung.de/SiteGlobals/Frontend/Images/favicon.ico?__blob=normal&v=5"
)

internal fun Url.favicon(): Url {
    val overwrite = lookup[host]
    if (overwrite != null) {
        return Url(overwrite)
    }
    return URLBuilder(
        protocol = protocolOrNull,
        host = host,
        pathSegments = listOf("favicon.ico")
    ).build()
}
