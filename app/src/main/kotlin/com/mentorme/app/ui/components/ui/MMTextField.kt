package com.mentorme.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mentorme.app.ui.theme.Border        // --border
import com.mentorme.app.ui.theme.Foreground    // --foreground (~rgba(255,255,255,0.95))
import com.mentorme.app.ui.theme.InputBg       // --input-background (~rgba(255,255,255,0.15))

/**
 * Input “liquid-glass”: nền mờ, border trắng mờ.
 * Map từ CSS:
 *  - background: var(--input-background)
 *  - border: 1px solid var(--border)
 *  - text: var(--foreground)
 *  - placeholder: foreground * 0.6 (nhạt)
 */
@Composable
fun MMTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,/**/
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(InputBg, shape)  // --input-background
            .border(1.dp, Border, shape),// --border
        enabled = enabled,
        placeholder = {
            if (placeholder != null) {
                Text(placeholder, color = Foreground.copy(alpha = 0.6f))
            }
        },
        leadingIcon = leading,
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Foreground,
            focusedTextColor = Foreground,
            unfocusedTextColor = Foreground
        )
    )
}
