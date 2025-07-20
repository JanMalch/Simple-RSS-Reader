package io.github.janmalch.simplerssreader.ui.screens.reader

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp


@Composable
internal fun PagerIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    maxIndicators: Int = 5,
) {
    Row(
        modifier = modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (pageCount <= maxIndicators) {
            repeat(pageCount) { iteration ->
                val color = if (currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        } else {
            val midway = maxIndicators / 2
            val currentIndicator = if (currentPage < midway) currentPage
            else if (currentPage >= pageCount - midway) (maxIndicators - (pageCount - currentPage))
            else midway
            repeat(maxIndicators) { iteration ->
                val color = if (currentIndicator == iteration) Color.DarkGray else Color.LightGray
                val size by animateDpAsState(
                    when (iteration) {
                        // size only changes for first ...
                        0 -> {
                            if (currentPage < midway) 8.dp
                            else 6.dp
                        }
                        // ... or last indicator
                        maxIndicators - 1 -> {
                            if (currentPage >= (pageCount - (midway + 1))) 8.dp
                            else 6.dp
                        }

                        else -> 8.dp
                    }
                )
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(size)
                )
            }
        }
    }
}

private class PagerIndicatorPreviewProvider : CollectionPreviewParameterProvider<Pair<Int, Int>>(
    List(8) { currentPage ->
        List(8) { pageCount ->
            if (currentPage >= pageCount) null
            else currentPage to pageCount
        }.filterNotNull()
    }.flatten()
)

@Preview(showBackground = true)
@Composable
private fun PagerIndicatorPreview(
    @PreviewParameter(PagerIndicatorPreviewProvider::class) data: Pair<Int, Int>
) {
    val (currentPage, pageCount) = data
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Text("${currentPage + 1} / $pageCount")
        PagerIndicator(currentPage, pageCount)
    }
}

