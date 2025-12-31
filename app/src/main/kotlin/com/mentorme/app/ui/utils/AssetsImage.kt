package com.mentorme.app.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun AssetImage(
    assetPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    val imageBitmap = remember(assetPath) {
        context.assets.open(assetPath).use {
            BitmapFactory.decodeStream(it).asImageBitmap()
        }
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
