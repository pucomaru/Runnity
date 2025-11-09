package com.example.runnity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    NavHost(
        navController = navController,
        startDestination = "welcome" // TODO: 로그인 상태 체크 후 "welcome" 또는 "main"으로 분기
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
