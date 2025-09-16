package com.mentorme.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mentorme.app.core.designsystem.MMGradients
import com.mentorme.app.core.designsystem.MMShapes
import com.mentorme.app.core.designsystem.MMColors
import com.mentorme.app.core.designsystem.mmGlass

/** Button có nền gradient giống --primary */
@Composable
fun MMButton(
    text: String,
    modifier: Modifier = Modifier,
    gradient: Brush = MMGradients.Primary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    // Button M3 không hỗ trợ Brush trực tiếp -> dựng nền gradient thủ công
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(MMShapes.RadiusMd))
            .background(brush = gradient)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MMColors.Foreground,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** TextField “liquid glass” */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MMTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        label = { if (label != null) Text(label) },
        placeholder = { if (placeholder != null) Text(placeholder) },
        modifier = modifier
            .mmGlass(cornerRadius = 16.dp)
            .heightIn(min = 48.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MMColors.InputBg,
            unfocusedContainerColor = MMColors.InputBg,
            disabledContainerColor = MMColors.InputBg.copy(alpha = 0.6f),
            focusedBorderColor = MMColors.Border,
            unfocusedBorderColor = MMColors.Border,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color(0xCCFFFFFF)
        ),
        visualTransformation = visualTransformation
    )
}

/** Card “liquid glass” */
@Composable
fun MMCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.mmGlass(),
        shape = RoundedCornerShape(MMShapes.RadiusLg),
        color = MMColors.Card,
        contentColor = MMColors.CardFg,
        tonalElevation = 8.dp
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}
