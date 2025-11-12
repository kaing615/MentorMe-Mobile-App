package com.mentorme.app.ui.calendar.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/*  Mask dd/MM/yyyy — input gốc CHỈ digits (đã filter ở onValueChange)  */
class DateMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(8)
        val rawLen = raw.length

        val filled = buildString {
            val pattern = charArrayOf('_','_','/','_','_','/','_','_','_','_')
            var i = 0
            raw.forEach { d ->
                while (i < pattern.size && pattern[i] == '/') { append('/'); i++ }
                if (i < pattern.size) { append(d); i++ }
            }
            while (i < pattern.size) { append(pattern[i]); i++ }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, rawLen)
                return when {
                    o <= 2 -> o
                    o <= 4 -> o + 1
                    else   -> (o + 2).coerceAtMost(10)
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                val guess = when {
                    offset <= 2  -> offset
                    offset <= 5  -> offset - 1
                    offset <= 10 -> offset - 2
                    else         -> 8
                }
                return guess.coerceIn(0, rawLen)  // CLAMP để tránh crash
            }
        }
        return TransformedText(AnnotatedString(filled), mapping)
    }
}

/*  Mask HH:mm — input gốc CHỈ digits  */
class TimeMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(4)
        val rawLen = raw.length

        val filled = buildString {
            val pattern = charArrayOf('_','_',':','_','_')
            var i = 0
            raw.forEach { d ->
                while (i < pattern.size && pattern[i] == ':') { append(':'); i++ }
                if (i < pattern.size) { append(d); i++ }
            }
            while (i < pattern.size) { append(pattern[i]); i++ }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, rawLen)
                return if (o <= 2) o else (o + 1).coerceAtMost(5)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val guess = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else        -> 4
                }
                return guess.coerceIn(0, rawLen)
            }
        }
        return TransformedText(AnnotatedString(filled), mapping)
    }
}
