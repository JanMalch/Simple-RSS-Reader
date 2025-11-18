package io.github.janmalch.simplerssreader.ui.screens.main

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.janmalch.shed.Shed
import io.github.janmalch.simplerssreader.R
import io.github.janmalch.simplerssreader.core.FeedItem
import io.github.janmalch.simplerssreader.core.FeedItemId
import kotlinx.serialization.Serializable

@Serializable
data object MainScreen : NavKey

private typealias OnItemClick = (item: FeedItemId, ids: List<FeedItemId>) -> Unit

@Composable
fun MainScreen(
    onGoToManageSources: () -> Unit,
    onItemClick: OnItemClick,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val feedItems by viewModel.feedItems.collectAsStateWithLifecycle()
    val isOnlyUnreadVisible by viewModel.isOnlyUnreadVisible.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    MainScreen(
        feedItems = feedItems,
        isOnlyUnreadVisible = isOnlyUnreadVisible,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        onMarkAllAsRead = viewModel::markAllAsRead,
        onToggleOnlyUnread = viewModel::toggleOnlyUnread,
        onItemClick = onItemClick,
        onGoToManageSources = onGoToManageSources,
        onGoToLicenses = {
            context.startActivity(
                Intent(
                    context,
                    OssLicensesMenuActivity::class.java
                )
            )
        },
        onGoToLogs = { Shed.startActivity(context) },
        modifier = modifier,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    feedItems: List<FeedItem>?,
    isOnlyUnreadVisible: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemClick: OnItemClick,
    onMarkAllAsRead: () -> Unit,
    onToggleOnlyUnread: () -> Unit,
    onGoToManageSources: () -> Unit,
    onGoToLicenses: () -> Unit,
    onGoToLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = rememberMainScreenDateFormatter()
    val scrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(stringResource(R.string.feed))
                    },
                    actions = {
                        IconButton(onClick = onToggleOnlyUnread) {
                            AnimatedContent(
                                targetState = isOnlyUnreadVisible,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                            ) {
                                if (it) Icon(
                                    Icons.Outlined.Visibility,
                                    contentDescription = stringResource(R.string.show_all)
                                )
                                else Icon(
                                    Icons.Outlined.VisibilityOff,
                                    contentDescription = stringResource(R.string.show_only_unread)
                                )
                            }
                        }
                        MoreMenu(
                            onGoToManageSources = onGoToManageSources,
                            onGoToLogs = onGoToLogs,
                            onGoToLicenses = onGoToLicenses,
                        )
                    }
                )
                AnimatedVisibility(
                    visible = feedItems == null,
                    label = "AnimatedVisibility:LinearProgressIndicator"
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onMarkAllAsRead) {
                Icon(
                    Icons.Default.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_as_read)
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (feedItems != null && feedItems.isEmpty()) {
                    item(
                        key = "NoItems",
                        contentType = "NoItems",
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.nothing_here))
                            }
                        )
                    }
                }
                feedItems?.also {
                items(
                    items = feedItems,
                    contentType = { "FeedItem" }
                ) { item ->
                    ListItem(
                        leadingContent = {
                            AsyncImage(
                                model = item.image,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Gray, CircleShape)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        },
                        overlineContent = {
                            Text(
                                text = item.author,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        headlineContent = {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Medium.takeUnless { item.isRead }
                            )
                        },
                        trailingContent = {
                            Text(
                                text = item.published
                                    ?.let { formatter.format(it) }
                                    .orEmpty()
                            )
                        },
                        modifier = Modifier
                            .animateItem()
                            .clickable { onItemClick(item.id, feedItems.map { it.id }) }
                    )
                    HorizontalDivider()
                }
                }
            }
        }
    }
}
