package com.mentorme.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// CSS root: --font-size:16px; h1..input lấy weight 400/500 và line-height ~1.5
val MentorMeTypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium, lineHeight = 36.sp), // h1
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 30.sp), // h2
    titleLarge    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 27.sp), // h3
    titleMedium   = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp), // h4/label/button
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp), // p/input
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp),
    labelLarge    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)
)
