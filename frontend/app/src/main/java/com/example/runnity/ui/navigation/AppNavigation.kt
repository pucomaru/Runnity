package com.example.runnity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.screens.login.LoginScreen
import com.example.runnity.ui.screens.login.OnboardingScreen
import com.example.runnity.ui.screens.login.ProfileSetupScreen
import com.example.runnity.ui.screens.login.WelcomeScreen

/**
 * 앱 최상위 네비게이션 구조
 * - 웰컴 → 온보딩 → 로그인 → 프로필 설정 → 메인
 * - 메인 탭 화면 (하단 네비게이션 바 포함)
 * - 추가 독립 화면들 (상세 페이지, 설정 등)
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AppNavigationViewModel = viewModel()
    val startDestination by viewModel.startDestination.collectAsState()

    // 앱 시작 시 자동으로 시작 화면 결정
    LaunchedEffect(Unit) {
        viewModel.checkStartDestination()
    }

    // 로딩 중 (시작 화면 결정 중)
    if (startDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.Light.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ColorPalette.Light.primary)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        // 웰컴 화면
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }

        // 온보딩 화면
        composable("onboarding") {
            OnboardingScreen(navController = navController)
        }

        // 로그인 화면
        composable("login") {
            LoginScreen(navController = navController)
        }

        // 프로필 설정 화면
        composable("profile_setup") {
            ProfileSetupScreen(navController = navController)
        }

        // 메인 탭 화면 (하단 네비게이션 바 포함)
        composable("main") {
            MainTabScreen(parentNavController = navController)
        }

        // TODO: 추가 화면들 (네비게이션 바 없는 독립 화면)
        // composable("running") { RunningScreen(navController) }
        // composable("challenge_detail/{id}") { ChallengeDetailScreen(navController) }
        // composable("settings") { SettingsScreen(navController) }
    }
}
