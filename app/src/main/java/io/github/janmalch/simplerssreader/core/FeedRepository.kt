package io.github.janmalch.simplerssreader.core

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.simplerssreader.network.AtomFeed
import io.github.janmalch.simplerssreader.network.RssFeed
import io.github.janmalch.simplerssreader.settings.FeedSource
import io.github.janmalch.simplerssreader.settings.Settings
import io.github.janmalch.simplerssreader.settings.settingsDataStore
import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.orEmpty
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

open class FeedException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

interface FeedRepository {
    fun watchSources(): Flow<List<Feed>>
    suspend fun add(url: Url): Feed
    suspend fun update(feed: Feed)
    suspend fun update()
    suspend fun delete(id: Uuid)
}

@Singleton
class AndroidFeedRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
) : FeedRepository {
    override fun watchSources(): Flow<List<Feed>> = context.settingsDataStore.data
        .map { it.sourcesList.orEmpty().map(FeedSource::toModel) }
        .distinctUntilChanged()

    override suspend fun add(url: Url): Feed {
        val data = httpClient.getFeed(url)
        val title = when (data) {
            is AtomFeed -> data.title
            is RssFeed -> data.channel.title
        }
        val id = Uuid.random()
        val source = FeedSource.newBuilder()
            .setId(id.toString())
            .setUrl(url.toString())
            .setTitle(title)
            .build()
        context.settingsDataStore.updateData {
            Settings.newBuilder(it)
                .addSources(source)
                .build()
        }
        return Feed(id, title, url)
    }

    override suspend fun update() {
        val existing = context.settingsDataStore.data.first().sourcesList.orEmpty()
        supervisorScope {
            existing.map {
                launch {
                    update(it.toModel())
                }
            }.joinAll()
        }
    }

    override suspend fun update(feed: Feed) {
        val idStr = feed.id.toString()
        val existing = context.settingsDataStore.data.first().sourcesList.orEmpty()
            .firstOrNull { it.id == idStr }
            ?: throw FeedException("No source found with ID '$idStr'.")

        val data = httpClient.getFeed(feed.url)
        val title = when (data) {
            is AtomFeed -> data.title
            is RssFeed -> data.channel.title
        }
        val source = FeedSource.newBuilder(existing)
            .setUrl(feed.url.toString())
            .setTitle(title)
            .build()
        context.settingsDataStore.updateData { settings ->
            Settings.newBuilder(settings)
                .setSources(settings.sourcesList.orEmpty().indexOfFirst { it.id == idStr }, source)
                .build()
        }
    }

    override suspend fun delete(id: Uuid) {
        val idStr = id.toString()
        context.settingsDataStore.updateData { settings ->
            Settings.newBuilder(settings)
                .removeSources(settings.sourcesList.orEmpty().indexOfFirst { it.id == idStr })
                .build()
        }
    }
}