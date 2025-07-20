package io.github.janmalch.simplerssreader.ui.screens.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.simplerssreader.core.Feed
import io.github.janmalch.simplerssreader.core.FeedRepository
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val feedsRepository: FeedRepository,
) : ViewModel() {

    val sources = feedsRepository.watchSources()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )

    fun add(url: Url) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Error while adding feed.")
        }) {
            feedsRepository.add(url)
        }
    }

    fun delete(feed: Feed) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Error while deleting feed.")
        }) {
            feedsRepository.delete(feed.id)
        }
    }
}