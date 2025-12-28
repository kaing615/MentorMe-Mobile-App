package com.mentorme.app.ui.components.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun glassOutlinedTextFieldColors(): TextFieldColors =
    TextFieldDefaults.colors(
        // Text & cursor
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White.copy(0.7f),
        errorTextColor = Color.White,
        cursorColor = Color.White,

        // Outline/border (indicator)
        focusedIndicatorColor = Color.White.copy(0.70f),
        unfocusedIndicatorColor = Color.White.copy(0.35f),
        disabledIndicatorColor = Color.White.copy(0.20f),
        errorIndicatorColor = Color.White.copy(0.70f),

        // Label colors
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(0.75f),
        disabledLabelColor = Color.White.copy(0.5f),
        errorLabelColor = Color.White,

        // Placeholder
        focusedPlaceholderColor = Color.White.copy(0.55f),
        unfocusedPlaceholderColor = Color.White.copy(0.55f),
        disabledPlaceholderColor = Color.White.copy(0.4f),

        // Icons
        focusedLeadingIconColor = Color.White,
        unfocusedLeadingIconColor = Color.White,
        disabledLeadingIconColor = Color.White.copy(0.5f),
        errorLeadingIconColor = Color.White,
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White,
        disabledTrailingIconColor = Color.White.copy(0.5f),
        errorTrailingIconColor = Color.White,

        // Transparent background for outlined fields
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent
    )

@Composable
fun glassButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(
        contentColor = Color.White
    )

@Composable
fun glassOutlinedButtonColors(): ButtonColors =
    ButtonDefaults.outlinedButtonColors(
        contentColor = Color.White
    )
