package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val BalooDa2FontFamily = FontFamily(
    Font(R.font.baloo_da_2, FontWeight.Normal),
    Font(R.font.baloo_da_2, FontWeight.Medium),
    Font(R.font.baloo_da_2, FontWeight.SemiBold),
    Font(R.font.baloo_da_2, FontWeight.Bold)
)

val HindSiliguriFontFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold)
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = BalooDa2FontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BalooDa2FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = BalooDa2FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 31.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BalooDa2FontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelSmall = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
