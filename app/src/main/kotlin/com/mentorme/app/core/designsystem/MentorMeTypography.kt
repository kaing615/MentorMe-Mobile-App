package com.mentorme.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.mentorme.app.R

/**
 * MentorMeTypography
 * - Map từ globals.css:
 *   h1: var(--text-2xl), fw 500
 *   h2: var(--text-xl),  fw 500
 *   h3: var(--text-lg),  fw 500
 *   h4: var(--text-base),fw 500
 *   p : var(--text-base),fw 400
 *   label/button/input: var(--text-base), fw tương ứng (label/button 500, input 400)
 *
 * Ghi chú: size mặc định đã quy đổi:
 *   --text-base = 16sp, --text-lg = 18sp, --text-xl = 20sp, --text-2xl = 24sp
 */


private val InterFamily = FontFamily(
    // Variable font: cùng một file, map nhiều weight
    Font(R.font.inter_variable, weight = FontWeight.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Medium),
    Font(R.font.inter_variable, weight = FontWeight.SemiBold),
    Font(R.font.inter_variable, weight = FontWeight.Bold),

    // Italic (nếu cần)
    Font(
        resId = R.font.inter_variable_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    )
)



// ====== Size tokens ======
private val TextBase = 16.sp
private val TextLg   = 18.sp
private val TextXl   = 20.sp
private val Text2Xl  = 24.sp

// ====== Weight tokens ======
private val WeightMedium = FontWeight.Medium // ~ 500
private val WeightNormal = FontWeight.Normal // ~ 400

object MentorMeTextStyles {
    // Headings
    val H1 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = Text2Xl,
        lineHeight = (Text2Xl.value * 1.5f).sp // line-height: 1.5
    )
    val H2 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = TextXl,
        lineHeight = (TextXl.value * 1.5f).sp
    )
    val H3 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = TextLg,
        lineHeight = (TextLg.value * 1.5f).sp
    )
    val H4 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = TextBase,
        lineHeight = (TextBase.value * 1.5f).sp
    )

    // Body paragraph (p)
    val P = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightNormal,
        fontSize = TextBase,
        lineHeight = (TextBase.value * 1.5f).sp
    )

    // Label
    val Label = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = TextBase,
        lineHeight = (TextBase.value * 1.5f).sp
    )

    // Button
    val Button = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightMedium,
        fontSize = TextBase,
        lineHeight = (TextBase.value * 1.5f).sp
    )

    // Input (text field)
    val Input = TextStyle(
        fontFamily = InterFamily,
        fontWeight = WeightNormal,
        fontSize = TextBase,
        lineHeight = (TextBase.value * 1.5f).sp
    )
}

/**
 * Material3 Typography map vào các slot phổ biến
 * (Em vẫn có thể tham chiếu MentorMeTextStyles trực tiếp khi cần “đúng chuẩn CSS”)
 */
val MentorMeTypography: Typography = Typography(
    displayLarge = MentorMeTextStyles.H1,     // h1
    headlineLarge = MentorMeTextStyles.H2,    // h2
    titleLarge = MentorMeTextStyles.H3,       // h3
    titleMedium = MentorMeTextStyles.H4,      // h4
    bodyLarge = MentorMeTextStyles.P,         // p
    labelLarge = MentorMeTextStyles.Label     // dùng cho label/button mặc định
    // Có thể bổ sung bodyMedium, labelMedium... khi cần.
)

/* ---------- Helper composables (gọi nhanh) ---------- */

@Composable
fun H1(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) = Text(text = text, style = MentorMeTextStyles.H1, color = color, modifier = modifier)

@Composable
fun H2(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) = Text(text = text, style = MentorMeTextStyles.H2, color = color, modifier = modifier)

@Composable
fun H3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) = Text(text = text, style = MentorMeTextStyles.H3, color = color, modifier = modifier)

@Composable
fun H4(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) = Text(text = text, style = MentorMeTextStyles.H4, color = color, modifier = modifier)

@Composable
fun P(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) = Text(text = text, style = MentorMeTextStyles.P, color = color, modifier = modifier)

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) = Text(text = text, style = MentorMeTextStyles.Label, color = color, modifier = modifier)

@Composable
fun ButtonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimary
) = Text(text = text, style = MentorMeTextStyles.Button, color = color, modifier = modifier)

@Composable
fun InputText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) = Text(text = text, style = MentorMeTextStyles.Input, color = color, modifier = modifier)
