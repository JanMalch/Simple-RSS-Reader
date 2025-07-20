package io.github.janmalch.simplerssreader.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(AppTypeConverters::class)
@Database(
    entities = [FeedItemEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedItemDao(): FeedItemDao

    companion object {
        fun create(context: Context, name: String = "app-database") = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            name = name,
        )
            // Settings (feed sources) are stored outside the database,
            // so we can nuke the database, if something goes wrong.
            // Actually, the settings are stored outside to enable exactly this.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

