package com.example.runnity.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PrimaryButton

/**
 * 로그인 화면
 * - Google 소셜 로그인
 * - Kakao 소셜 로그인
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고
        Image(
            painter = painterResource(id = R.drawable.runnity_logo),
            contentDescription = "Runnity Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        // 환영 메시지
        Text(
            text = "러니티에 오신 것을 환영합니다",
            style = Typography.Title,
            color = ColorPalette.Light.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "소셜 로그인으로 간편하게 시작하세요",
            style = Typography.Body,
            color = ColorPalette.Light.component,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Google 로그인 버튼
        PrimaryButton(
            text = "Google로 시작하기",
            onClick = {
                // TODO: Google OAuth 로그인 처리
                navController.navigate("profile_setup")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
            modifier = Modifier.offset(y = 4.dp)
        )

        // Kakao 로그인 버튼
        PrimaryButton(
            text = "Kakao로 시작하기",
            onClick = {
                // TODO: Kakao OAuth 로그인 처리
                navController.navigate("profile_setup")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE500),  // Kakao yellow
                contentColor = Color.Black
            ),
            modifier = Modifier.offset(y = (-4).dp)
        )
    }
}
