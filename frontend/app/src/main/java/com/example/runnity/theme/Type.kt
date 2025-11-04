package com.example.runnity.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.runnity.R

// Runnity 디자인 시스템 텍스트 스타일
object Typography {
    // Pretendard 폰트 패밀리
    private val PretendardFontFamily = FontFamily(
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_bold, FontWeight.Bold)
    )
    // LargeTitle - 36px, 700 Bold
    val LargeTitle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    )

    // Title - 24px, 700 Bold
    val Title = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )

    // Heading - 20px, 700 Bold
    val Heading = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    // Subtitle - 18px, 700 Bold
    val Subtitle = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )

    // Subheading - 16px, 700 Bold
    val Subheading = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    // Body - 14px, 500 Medium
    val Body = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )

    // Caption - 12px, 500 Medium
    val Caption = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )

    // Caption_small - 11px, 500 Medium
    val CaptionSmall = TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
}