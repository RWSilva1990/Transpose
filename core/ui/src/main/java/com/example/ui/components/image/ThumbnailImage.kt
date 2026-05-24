package com.example.ui.components.image

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.example.util.constants.AppColors

@Composable
fun ThumbnailImage(
    url: String?,
    contentDescription: String?,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fillMaxSize: Boolean = false,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val request = remember(url, width, height) {
        val widthPx = with(density) { width.roundToPx() }
        val heightPx = with(density) { height.roundToPx() }
        ImageRequest.Builder(context)
            .data(url)
            .size(widthPx, heightPx)
            .precision(Precision.INEXACT)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = if (fillMaxSize) {
            Modifier.fillMaxSize().then(modifier)
        } else {
            Modifier.size(width = width, height = height).then(modifier)
        },
        contentScale = contentScale,
        placeholder = ColorPainter(AppColors.LightGray),
        error = ColorPainter(AppColors.LightGray),
    )
}
