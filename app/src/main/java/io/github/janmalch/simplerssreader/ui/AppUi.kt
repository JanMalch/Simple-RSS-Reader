package io.github.janmalch.simplerssreader.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import io.github.janmalch.simplerssreader.ui.screens.main.MainScreen
import io.github.janmalch.simplerssreader.ui.screens.reader.ReaderScreen
import io.github.janmalch.simplerssreader.ui.screens.reader.ReaderViewModel
import io.github.janmalch.simplerssreader.ui.screens.sources.SourcesScreen
import io.github.janmalch.simplerssreader.ui.theme.SimpleRSSReaderTheme

@Composable
fun AppUi() {
    SimpleRSSReaderTheme {
        val backStack = remember { mutableStateListOf<NavKey>(MainScreen) }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                when (key) {
                    is MainScreen -> NavEntry(key) {
                        MainScreen(
                            onItemClick = { item, isOnlyUnreadVisible ->
                                backStack.add(
                                    ReaderScreen(
                                        initialItem = item,
                                        isOnlyUnreadVisible = isOnlyUnreadVisible,
                                    )
                                )
                            },
                            onGoToManageSources = {
                                backStack.add(SourcesScreen)
                            }
                        )
                    }

                    is SourcesScreen -> NavEntry(key) {
                        SourcesScreen(
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    is ReaderScreen -> NavEntry(key) {
                        ReaderScreen(
                            viewModel = hiltViewModel<ReaderViewModel, ReaderViewModel.Factory>(
                                creationCallback = { factory ->
                                    factory.create(key)
                                }
                            ),
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }

                    else -> NavEntry(key) { Text("?") }
                }
            }
        )
    }
}
