package com.example.runnity.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 네비게이션 아이템 정의
 *
 * sealed class란?
 * - 제한된 클래스 계층 구조를 만드는 특별한 클래스
 * - Home, StartRun, Challenge, MyPage만 존재 가능 (다른 건 못 만듦)
 * - when 문에서 모든 케이스를 확인할 수 있어서 안전함
 */
sealed class BottomNavItem(
    val route: String,        // 시작 화면 경로 (예: "home", "challenge")
    val graphRoute: String,   // 이 탭에 속한 여러 화면들의 그룹 이름 (예: "home_graph")
    val label: String,        // 하단 네비바에 보이는 텍스트 (예: "홈")
    val selectedIcon: ImageVector,    // 탭이 선택됐을 때 아이콘
    val unselectedIcon: ImageVector   // 탭이 선택 안 됐을 때 아이콘
) {
    // object란? 싱글톤 객체 (앱에서 딱 하나만 존재)
    object Home : BottomNavItem(
        route = "home",              // 홈 화면 경로
        graphRoute = "home_graph",   // 홈 그래프 (홈 + 챌린지 세부 화면들)
        label = "홈",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Filled.Home
    )

    object StartRun : BottomNavItem(
        route = "start_run",
        graphRoute = "start_run_graph",
        label = "개인",
        selectedIcon = Icons.AutoMirrored.Filled.DirectionsRun,
        unselectedIcon = Icons.AutoMirrored.Filled.DirectionsRun
    )

    object Challenge : BottomNavItem(
        route = "challenge",         // 챌린지 리스트 화면 경로
        graphRoute = "challenge_graph",  // 챌린지 그래프 (리스트 + 세부 화면들)
        label = "챌린지",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Filled.Group
    )

    object MyPage : BottomNavItem(
        route = "mypage",
        graphRoute = "mypage_graph",
        label = "마이페이지",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Filled.Person
    )
}

// 하단 네비 아이템 리스트
val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.StartRun,
    BottomNavItem.Challenge,
    BottomNavItem.MyPage
)
