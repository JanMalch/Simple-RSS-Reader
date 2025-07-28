package io.github.janmalch.simplerssreader.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.simplerssreader.core.FeedItemRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val itemsRepository: FeedItemRepository
) : ViewModel() {

    private val onlyUnread = MutableStateFlow(true)
    val isOnlyUnreadVisible = onlyUnread.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingItems = onlyUnread.flatMapLatest {
        itemsRepository.paginateAll(onlyUnread = it)
    }.cachedIn(viewModelScope)

    fun toggleOnlyUnread() {
        onlyUnread.value = !onlyUnread.value
    }

    fun markAllAsRead() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Error while marking all items as read.")
        }) {
            itemsRepository.markAllAsRead()
        }
    }

    fun refresh() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Unexpected error while refreshing all feeds.")
        }) {
            _isRefreshing.value = true
            yield()
            try {
                itemsRepository.update()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}