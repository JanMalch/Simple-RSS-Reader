package io.github.janmalch.simplerssreader.ui.screens.reader

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.i18n.DateTimeFormatter
import androidx.core.i18n.DateTimeFormatterJdkStyleOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import io.github.janmalch.simplerssreader.R
import io.github.janmalch.simplerssreader.core.FeedItem
import io.github.janmalch.simplerssreader.core.FeedItemId
import io.github.janmalch.simplerssreader.ui.SuppressLinks
import kotlinx.serialization.Serializable
import java.text.DateFormat

@Serializable
data class ReaderScreen(
    val initialItem: FeedItemId,
    val isOnlyUnreadVisible: Boolean,
) : NavKey

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReaderScreen(
        uiState = uiState,
        onMarkAsRead = viewModel::markAsRead,
        onMarkAsUnread = viewModel::markAsUnread,
        onBack = onBack,
        modifier = modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    uiState: UiState,
    onMarkAsRead: (FeedItemId) -> Unit,
    onMarkAsUnread: (FeedItemId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = when (uiState) {
        is UiState.Success -> rememberPagerState(uiState.initialIndex) {
            uiState.items.size
        }

        else -> null
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(
                                R.string.back
                            )
                        )
                    }
                },
                title = {
                    pagerState?.also {
                        PagerIndicator(
                            currentPage = pagerState.currentPage,
                            pageCount = pagerState.pageCount,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                actions = {
                    InfoAction(uiState, pagerState)
                }
            )
        },
        floatingActionButton = {
            if (pagerState == null || uiState !is UiState.Success || uiState.items.isEmpty()) return@Scaffold
            val uriHandler = LocalUriHandler.current
            val currentUrl =
                uiState.items[pagerState.currentPage].url?.toString() ?: return@Scaffold
            FloatingActionButton(onClick = {
                uriHandler.openUri(currentUrl)
            }) {
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = stringResource(R.string.open_link)
                )
            }

        }
    ) { innerPadding ->
        when (uiState) {
            UiState.Failure -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Text(
                        text = "\uD83D\uDE22",
                        style = MaterialTheme.typography.displayLarge,
                    )
                }
            }

            UiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Success ->
                pagerState?.also {
                    ItemPager(
                        items = uiState.items,
                        pagerState = pagerState,
                        onMarkAsRead = onMarkAsRead,
                        onMarkAsUnread = onMarkAsUnread,
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxSize()
                    )
                }
        }
    }
}

private val dtf = DateTimeFormatter(
    options = DateTimeFormatterJdkStyleOptions.createDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.MEDIUM,
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoAction(
    uiState: UiState,
    pagerState: PagerState?,
) {
    var selected by remember { mutableStateOf<FeedItem?>(null) }
    IconButton(
        enabled = pagerState != null,
        onClick = {
            if (pagerState == null) return@IconButton
            when (val state = uiState) {
                is UiState.Success -> {
                    selected = state.items[pagerState.currentPage]
                }

                else -> {}
            }
        }
    ) {
        Icon(Icons.Outlined.Info, "Info")
    }

    selected?.also { item ->
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                InfoRow(
                    icon = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.author),
                    text = item.author
                )
                InfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    contentDescription = stringResource(R.string.published),
                    text = item.published?.toEpochMilliseconds()?.let { dtf.format(it) }
                        .orEmpty()
                )
                InfoRow(
                    icon = Icons.Outlined.Link,
                    contentDescription = stringResource(R.string.url),
                    text = item.url?.toString().orEmpty()
                )
                InfoRow(
                    icon = Icons.Outlined.DataArray,
                    contentDescription = stringResource(R.string.content_type),
                    text = when (item.content) {
                        is FeedItem.Content.Html -> stringResource(R.string.html)
                        is FeedItem.Content.Image -> stringResource(R.string.image)
                        is FeedItem.Content.Link -> stringResource(R.string.link)
                        is FeedItem.Content.Unknown -> stringResource(R.string.unknown)
                        is FeedItem.Content.Video -> stringResource(R.string.video)
                        is FeedItem.Content.YouTube -> stringResource(R.string.youtube)
                        is FeedItem.Content.YouTubeShort -> stringResource(R.string.youtube_short)
                    }
                )
                InfoRow(
                    icon = Icons.Outlined.Tag,
                    contentDescription = stringResource(R.string.id),
                    text = item.id.id,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    contentDescription: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(icon, contentDescription, Modifier.alpha(0.6f))
        Text(text = text)
    }
}

private val textLinkStyles = TextLinkStyles(
    style = SpanStyle(
        textDecoration = TextDecoration.Underline,
    )
)

@Composable
private fun ItemPager(
    items: List<FeedItem>,
    pagerState: PagerState,
    onMarkAsRead: (FeedItemId) -> Unit,
    onMarkAsUnread: (FeedItemId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { it },
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1,
        ) { index ->
            val item = items[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(contentPadding),
            ) {
                Text(
                    text = AnnotatedString.fromHtml(
                        item.title,
                        linkInteractionListener = SuppressLinks,
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                ContentDisplay(item.content)
            }

            LaunchedEffect(Unit) {
                onMarkAsRead(item.id)
            }
        }
    }
}

private val YouTubeRed = Color(255, 0, 51)

@Composable
private fun ContentDisplay(
    content: FeedItem.Content,
) {

    when (content) {
        is FeedItem.Content.Image -> {
            Box(modifier = Modifier.padding(24.dp)) {
                AsyncImage(
                    model = content.url.toString(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is FeedItem.Content.Link -> {
            val uriHandler = LocalUriHandler.current

            Box(modifier = Modifier.padding(24.dp)) {
                TextButton(
                    onClick = { uriHandler.openUri(content.url.toString()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(content.url.toString())
                }

            }
        }

        is FeedItem.Content.YouTubeShort -> {
            val uriHandler = LocalUriHandler.current

            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { uriHandler.openUri(content.url.toString()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YouTubeRed,
                    ),
                ) {
                    Text(stringResource(R.string.open_youtube_short))
                }

            }
        }

        is FeedItem.Content.YouTube -> {
            val uriHandler = LocalUriHandler.current

            Box(modifier = Modifier.padding(24.dp)) {
                Button(
                    onClick = { uriHandler.openUri(content.url.toString()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YouTubeRed,
                    ),
                ) {
                    Text(stringResource(R.string.open_youtube_video))
                }

            }
        }

        is FeedItem.Content.Html -> {

            Box(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(
                        content.text,
                        textLinkStyles,
                    )
                )

            }
        }

        is FeedItem.Content.Video -> {
            val contentColor = LocalContentColor.current
            val backgroundColor = MaterialTheme.colorScheme.surface
            // TODO: improve support
            AndroidView(
                factory = { WebView(it) },
                update = {
                    it.loadHtml(
                        html = "<video src=\"${content.url}\" controls>",
                        contentColor = contentColor,
                        backgroundColor = backgroundColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        is FeedItem.Content.Unknown -> {
            if (content.text == null) return
            val contentColor = LocalContentColor.current
            val backgroundColor = MaterialTheme.colorScheme.surface
            AndroidView(
                factory = { WebView(it) },
                update = {
                    it.loadHtml(
                        html = content.text,
                        contentColor = contentColor,
                        backgroundColor = backgroundColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
