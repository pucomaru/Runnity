package com.example.runnity.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.PrimaryButton

/**
 * 웰컴 화면
 * - 앱 최초 실행 시 보이는 화면
 * - 로고와 시작하기/건너뛰기 버튼
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun WelcomeScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Common.accent)
            .statusBarsPadding()
    ) {
        // 중앙 로고
        Image(
            painter = painterResource(id = R.drawable.runnity_logo),
            contentDescription = "Runnity Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp),
            contentScale = ContentScale.Fit
        )

        // 하단 버튼들
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 시작하기 버튼 (흰색 배경)
            PrimaryButton(
                text = "시작하기",
                onClick = {
                    navController.navigate("onboarding")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ColorPalette.Common.accent
                ),
                modifier = Modifier.offset(y = 4.dp)
            )

            // 건너뛰기 버튼 (투명 배경, 흰색 테두리)
            PrimaryButton(
                text = "건너뛰기",
                onClick = {
                    navController.navigate("login")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.offset(y = (-4).dp)
            )
        }
    }
}
