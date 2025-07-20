package io.github.janmalch.simplerssreader.ui.screens.sources

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import io.github.janmalch.simplerssreader.R
import io.github.janmalch.simplerssreader.core.Feed
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import timber.log.Timber


@Serializable
data object SourcesScreen : NavKey

@Composable
fun SourcesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = hiltViewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    SourcesScreen(
        sources = sources,
        onBack = onBack,
        onAdd = viewModel::add,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    sources: List<Feed>?,
    onBack: () -> Unit,
    onAdd: (url: Url) -> Unit,
    onDelete: (feed: Feed) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isNewDialogVisible by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.sources))
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isNewDialogVisible = true
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_feed)
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            sources?.fastForEach { source ->
                var isDeleteDialogVisible by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = {
                        Text(source.title)
                    },
                    supportingContent = {
                        Text(source.url.toString(), maxLines = 1)
                    },
                    modifier = Modifier.clickable {
                        isDeleteDialogVisible = true
                    }
                )
                HorizontalDivider()
                if (isDeleteDialogVisible) {
                    DeleteFeedDialog(
                        feed = source,
                        onDismissRequest = { isDeleteDialogVisible = false },
                        onDelete = onDelete,
                    )
                }
            }
        }
        if (isNewDialogVisible) {
            NewFeedDialog(
                onDismissRequest = { isNewDialogVisible = false },
                onSave = onAdd,
            )
        }
    }
}

@Composable
private fun NewFeedDialog(
    onDismissRequest: () -> Unit,
    onSave: (url: Url) -> Unit,
) {
    var formUrl by remember { mutableStateOf("") }
    val isUrlError = formUrl.let { it.isBlank() || runCatching { Url(it) }.isFailure }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(Icons.Outlined.RssFeed, contentDescription = null)
        },
        title = {
            Text(stringResource(R.string.new_feed))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextField(
                    value = formUrl,
                    onValueChange = { formUrl = it },
                    label = {
                        Text(stringResource(R.string.url))
                    },
                    singleLine = false,
                    isError = isUrlError,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isUrlError) return@TextButton
                    onSave(Url(formUrl))
                    onDismissRequest()
                },
                enabled = !isUrlError,
            ) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun DeleteFeedDialog(
    feed: Feed,
    onDismissRequest: () -> Unit,
    onDelete: (feed: Feed) -> Unit,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val clipDataLabel = stringResource(R.string.feed_url)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(Icons.Outlined.Delete, contentDescription = null)
        },
        title = {
            Text(stringResource(R.string.delete_feed))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.delete_feed_confirm, feed.title))
                TextField(
                    value = feed.url.toString(),
                    onValueChange = { },
                    label = {
                        Text(stringResource(R.string.url))
                    },
                    singleLine = false,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
                                Timber.e(throwable, "Failed to copy feed URL '%s'.", feed.url)
                            }) {
                                clipboard.setClipEntry(ClipEntry(
                                    ClipData.newPlainText(clipDataLabel, feed.url.toString())
                                ))
                            }
                        }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.copy_feed_url))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete(feed)
                    onDismissRequest()
                },
            ) {
                Text(stringResource(R.string.delete))
            }
        },
    )
}