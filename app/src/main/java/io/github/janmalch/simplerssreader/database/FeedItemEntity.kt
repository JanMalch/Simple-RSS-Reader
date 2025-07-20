package io.github.janmalch.simplerssreader.database

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.github.janmalch.simplerssreader.core.FeedItemHint
import io.github.janmalch.simplerssreader.core.FeedItemId
import io.ktor.http.Url
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val FeedItemEntityTableName = "feed_item"
internal const val FeedItemEntityPublishedColumnName = "published"

@Entity(tableName = FeedItemEntityTableName)
data class FeedItemEntity(
    @PrimaryKey val id: FeedItemId,
    val source: Uuid,
    val title: String,
    val author: String,
    val content: String?,
    val url: Url?,
    @ColumnInfo(FeedItemEntityPublishedColumnName)
    val published: Instant?,
    val isRead: Boolean,
    val image: Url?,
    val hint: FeedItemHint?,
    val embed: Url?,
)

@Dao
interface FeedItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(items: List<FeedItemEntity>)

    // TODO: share queries as const vals

    @Query("SELECT * FROM feed_item ORDER BY published DESC")
    fun paginateAll(): PagingSource<Int, FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE source = :source ORDER BY published DESC")
    fun paginateAll(source: Uuid): PagingSource<Int, FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE isRead = 0 ORDER BY published DESC")
    fun paginateUnread(): PagingSource<Int, FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE isRead = 0 AND source = :source ORDER BY published DESC")
    fun paginateUnread(source: Uuid): PagingSource<Int, FeedItemEntity>

    @Query("SELECT * FROM feed_item ORDER BY published DESC")
    suspend fun findAll(): List<FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE source = :source ORDER BY published DESC")
    suspend fun findAll(source: Uuid): List<FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE isRead = 0 ORDER BY published DESC")
    suspend fun findUnread(): List<FeedItemEntity>

    @Query("SELECT * FROM feed_item WHERE isRead = 0 AND source = :source ORDER BY published DESC")
    suspend  fun findUnread(source: Uuid): List<FeedItemEntity>

    @Query("UPDATE feed_item SET isRead = 1 WHERE id IN (:id)")
    suspend fun markAsRead(id: List<FeedItemId>)

    @Query("UPDATE feed_item SET isRead = 0 WHERE id IN (:id)")
    suspend fun markAsUnread(id: List<FeedItemId>)

    @Query("UPDATE feed_item SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE feed_item SET isRead = 0")
    suspend fun markAllAsUnread()

    @Query("SELECT COUNT(1) FROM feed_item WHERE isRead = 0")
    suspend fun countUnread(): Int

    @Query("DELETE FROM feed_item")
    suspend fun deleteAll()
}