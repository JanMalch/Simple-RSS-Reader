package io.github.janmalch.simplerssreader.core

import io.github.janmalch.simplerssreader.database.FeedItemDao
import io.github.janmalch.simplerssreader.network.AtomFeed
import io.github.janmalch.simplerssreader.network.RssFeed
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface FeedItemRepository {
    fun findAll(onlyUnread: Boolean = true): Flow<List<FeedItem>>

    suspend fun findAll(ids: List<FeedItemId>): List<FeedItem>

    suspend fun deleteAll()
    suspend fun update()
    suspend fun countUnread(): Int

    suspend fun markAsRead(id: List<FeedItemId>)
    suspend fun markAsRead(id: FeedItemId) = markAsRead(listOf(id))

    suspend fun markAsUnread(id: List<FeedItemId>)
    suspend fun markAsUnread(id: FeedItemId) = markAsUnread(listOf(id))

    suspend fun markAllAsRead()

    suspend fun markAllAsUnread()
}

private const val TAG = "AndroidFeedItemRepository"

@Singleton
class AndroidFeedItemRepository @Inject constructor(
    private val dao: FeedItemDao,
    private val feeds: FeedRepository,
    private val httpClient: HttpClient,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineContext,
) : FeedItemRepository {

    private val crudMutex = Mutex()

    override suspend fun countUnread(): Int = dao.countUnread()

    override suspend fun deleteAll() = crudMutex.withLock {
        dao.deleteAll()
    }

    override suspend fun findAll(ids: List<FeedItemId>): List<FeedItem> =
        dao.findAll(ids)
            .sortedBy { ids.indexOf(it.id) }
            .map { it.toModel() }

    override fun findAll(onlyUnread: Boolean): Flow<List<FeedItem>> =
        if (onlyUnread) {
            dao.findUnread()
        } else {
            dao.findAll()
        }
            .map { list ->
                list.map { it.toModel() }
            }

    override suspend fun update(): Unit = crudMutex.withLock {
        val feeds = feeds.watchSources().first()
        if (feeds.isEmpty()) {
            return
        }
        withContext(defaultDispatcher) {
            coroutineScope {
                val results = feeds.map {
                    async {
                        try {
                            Result.success(update(it))
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Timber.tag(TAG)
                                .w(e, "Error while updating feed '%s' from %s.", it.title, it.url)
                            Result.failure(e)
                        }
                    }
                }.awaitAll()
                if (results.all { it.isFailure }) {
                    throw IllegalStateException(
                        "All ${results.size} feed updates failed.",
                        results.find { it.isFailure }?.exceptionOrNull()
                    )
                }
                Timber.tag(TAG).i(
                    "Updating feeds has finished with %d successful results and %d failures.",
                    results.count { it.isSuccess },
                    results.count { it.isFailure })
            }
        }
    }

    private suspend fun update(feed: Feed) {
        Timber.tag(TAG).i("Updating feed '%s' from %s.", feed.title, feed.url)
        val data = httpClient.getFeed(feed.url)
        // TODO: update title?
        val entities = when (data) {
            is AtomFeed -> data.toEntities(feed.id)
            is RssFeed -> data.toEntities(feed.id)
        }
        Timber.tag(TAG).i("Got %d items from feed '%s'.", entities.size, feed.title)
        if (entities.isEmpty()) {
            return
        }
        dao.insert(entities)
    }

    override suspend fun markAsRead(id: List<FeedItemId>) {
        dao.markAsRead(id)
    }

    override suspend fun markAsUnread(id: List<FeedItemId>) {
        dao.markAsUnread(id)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    override suspend fun markAllAsUnread() {
        dao.markAllAsUnread()
    }
}
