package com.magnetar.janus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Technical = FontFamily.Monospace
private val Humanist = FontFamily.SansSerif

val Typography =
    Typography(
        displaySmall = TextStyle(fontFamily = Technical, fontWeight = FontWeight.Medium, fontSize = 30.sp, letterSpacing = 1.sp),
        headlineSmall = TextStyle(fontFamily = Humanist, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
        titleMedium = TextStyle(fontFamily = Humanist, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        bodyLarge = TextStyle(fontFamily = Humanist, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = Humanist, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = Technical, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.sp),
        labelSmall = TextStyle(fontFamily = Technical, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 1.2.sp),
    )
