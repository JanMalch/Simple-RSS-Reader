package io.github.janmalch.simplerssreader.network

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.cio.CIO
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun providesHttpClient(@ApplicationContext context: Context) = createHttpClient(
        engine = CIO.create(),
        cache = File(context.cacheDir, "http")
    )
}
