package io.github.janmalch.simplerssreader.core

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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
import kotlin.uuid.Uuid

interface FeedItemRepository {
    fun paginateAll(
        onlyUnread: Boolean = true,
        source: Uuid? = null,
        config: PagingConfig = PagingConfig(pageSize = 20)
    ): Flow<PagingData<FeedItem>>

    suspend fun findAll(
        onlyUnread: Boolean = true,
        source: Uuid? = null,
    ): List<FeedItem>

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

    override suspend fun findAll(
        onlyUnread: Boolean,
        source: Uuid?,
    ): List<FeedItem> {
        val entities =
            if (onlyUnread) {
                if (source == null) dao.findUnread()
                else dao.findUnread(source)
            } else {
                if (source == null) dao.findAll()
                else dao.findAll(source)
            }
        return entities.map { it.toModel() }
    }

    override fun paginateAll(
        onlyUnread: Boolean,
        source: Uuid?,
        config: PagingConfig
    ): Flow<PagingData<FeedItem>> =
        Pager(
            config = config,
        ) {
            if (onlyUnread) {
                if (source == null) dao.paginateUnread()
                else dao.paginateUnread(source)
            } else {
                if (source == null) dao.paginateAll()
                else dao.paginateAll(source)
            }
        }
            .flow
            .map { pagingData ->
                pagingData.map { it.toModel() }
            }

    override suspend fun update(): Unit = crudMutex.withLock {
        val feeds = feeds.watchSources().first()
        if (feeds.isEmpty()) {
            return
        }
        withContext(defaultDispatcher) {
            coroutineScope {
                feeds.map {
                    async {
                        update(it)
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun update(feed: Feed) {
        Timber.tag(TAG).i("Updating feed '%s' from %s.", feed.title, feed.url)
        try {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error while updating feed '%s' from %s.", feed.title, feed.url)
            throw e
        }
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
