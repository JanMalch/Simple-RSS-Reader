package io.github.janmalch.simplerssreader.database

import androidx.room.TypeConverter
import io.github.janmalch.simplerssreader.core.FeedItemHint
import io.github.janmalch.simplerssreader.core.FeedItemId
import io.ktor.http.Url
import kotlin.time.Instant
import kotlin.uuid.Uuid

object AppTypeConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(value) }

    @TypeConverter
    fun fromFeedItemId(value: FeedItemId?): String? = value?.id
    @TypeConverter
    fun toFeedItemId(value: String?): FeedItemId? = value?.let { FeedItemId(it) }

    @TypeConverter
    fun fromUrl(value: Url?): String? = value?.toString()
    @TypeConverter
    fun toUrl(value: String?): Url? = value?.let { Url(it) }

    @TypeConverter
    fun fromUuid(value: Uuid?): String? = value?.toString()
    @TypeConverter
    fun toUuid(value: String?): Uuid? = value?.let { Uuid.parse(it) }

    @TypeConverter
    fun fromFeedItemHint(value: FeedItemHint?): String? = value?.value

    @TypeConverter
    fun toFeedItemHint(value: String?): FeedItemHint? = value?.let { FeedItemHint.parse(it) }
}