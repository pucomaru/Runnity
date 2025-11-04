package com.example.runnity.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 네비게이션 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        label = "홈",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Filled.Home
    )

    object StartRun : BottomNavItem(
        route = "start_run",
        label = "개인",
        selectedIcon = Icons.AutoMirrored.Filled.DirectionsRun,
        unselectedIcon = Icons.AutoMirrored.Filled.DirectionsRun
    )

    object Challenge : BottomNavItem(
        route = "challenge",
        label = "챌린지",
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Filled.Group
    )

    object MyPage : BottomNavItem(
        route = "mypage",
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
