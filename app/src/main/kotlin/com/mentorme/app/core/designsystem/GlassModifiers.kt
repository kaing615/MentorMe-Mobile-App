package com.mentorme.app.core.designsystem

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Các modifier “glass” tương đương .glass / .glass-hover / .glass-strong trong CSS.
 * Lưu ý: blur chỉ thật sự tốt từ API 31+. Ở API thấp hơn, hiệu ứng sẽ nhẹ hơn.
 */

fun Modifier.mmGlass(
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = MMShapes.RadiusLg
) = this
    .background(MMColors.GlassBg)
    .border(borderWidth, MMColors.GlassBorder, shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
    .then(
        if (Build.VERSION.SDK_INT >= 31)
            this.blur(8.dp, BlurredEdgeTreatment.Unbounded)
        else this
    )

fun Modifier.mmGlassStrong(
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = MMShapes.RadiusLg
) = this
    .background(MMColors.GlassBackdrop)
    .border(borderWidth, Color(0x4DFFFFFF), shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
    .then(
        if (Build.VERSION.SDK_INT >= 31)
            this.blur(20.dp, BlurredEdgeTreatment.Unbounded)
        else this
    )
    .shadow(20.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius), clip = false)
