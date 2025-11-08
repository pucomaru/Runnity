package com.example.runnity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 스크린 설치 (setContent 전에 호출!)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Material3 컴포넌트가 커스텀 색상을 사용하도록 최소한의 colorScheme 설정
            // 커스텀 ColorPalette를 Material3 colorScheme에 매핑
            val colorScheme = lightColorScheme(
                background = ColorPalette.Light.background,
                surface = ColorPalette.Light.background
            )

            MaterialTheme(colorScheme = colorScheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorPalette.Light.background)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}