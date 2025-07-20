package io.github.janmalch.simplerssreader.ui.screens.reader

import android.webkit.WebView
import androidx.compose.ui.graphics.Color

private fun css(
    textColor: String,
    backgroundColor: String
) = """
    * {
        box-sizing: border-box;
    }
    
    html, body {
        width: 100%;
        margin: 0;
        padding: 0;
        color: $textColor;
        background-color: $backgroundColor;
    }
    
    body {
        padding: 1.5rem;
    }
    
    img, video {
        display: block;
        width: 100%;
    }
    
    p {
        line-height: 1.65;
    }
"""

internal fun WebView.loadHtml(
    html: String,
    contentColor: Color,
                              backgroundColor: Color,
) {
    loadDataWithBaseURL(
        null,
        "<html><head><style>${css(contentColor.toCssHex(), backgroundColor.toCssHex())}</style></head><body>$html</body></html>",
        "text/html; charset=utf-8",
        "UTF-8",
        null,
    )
}

private fun Color.toCssHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return         String.format("#%02X%02X%02X", r, g, b)

}