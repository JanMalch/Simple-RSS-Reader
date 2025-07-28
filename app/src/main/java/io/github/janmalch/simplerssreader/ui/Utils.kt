package io.github.janmalch.simplerssreader.ui

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener

internal object SuppressLinks : LinkInteractionListener {
    override fun onClick(link: LinkAnnotation) {
        // no-op
    }
}
