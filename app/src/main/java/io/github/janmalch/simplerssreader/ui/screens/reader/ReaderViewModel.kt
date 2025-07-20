package io.github.janmalch.simplerssreader.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.simplerssreader.core.FeedItem
import io.github.janmalch.simplerssreader.core.FeedItemId
import io.github.janmalch.simplerssreader.core.FeedItemRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds


@HiltViewModel(assistedFactory = ReaderViewModel.Factory::class)
class ReaderViewModel @AssistedInject constructor(
    @Assisted private val args: ReaderScreen,
    private val itemRepository: FeedItemRepository,
) : ViewModel() {


    val uiState = suspend {
        val items = itemRepository.findAll(onlyUnread = args.isOnlyUnreadVisible)
        val initialIndex = max(0, items.indexOfFirst { it.id == args.initialItem })
        UiState.Success(initialIndex, items)
    }.asFlow<UiState>()
        .catch {
            Timber.e(it, "Error while loading all items.")
            emit(UiState.Failure)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun markAsRead(id: FeedItemId) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Failed to mark item %s as read.", id)
        }) {
            itemRepository.markAsRead(id)
        }
    }

    fun markAsUnread(id: FeedItemId) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Failed to mark item %s as unread.", id)
        }) {
            itemRepository.markAsUnread(id)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(key: ReaderScreen): ReaderViewModel
    }
}

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val initialIndex: Int,
        val items: List<FeedItem>
    ) : UiState

    data object Failure : UiState
}