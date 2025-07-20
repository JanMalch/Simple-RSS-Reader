package io.github.janmalch.simplerssreader.core

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
interface CoreModule {
    @Binds
    fun bindsFeedRepository(impl: AndroidFeedRepository): FeedRepository
    @Binds
    fun bindsItemRepository(impl: AndroidFeedItemRepository): FeedItemRepository
}