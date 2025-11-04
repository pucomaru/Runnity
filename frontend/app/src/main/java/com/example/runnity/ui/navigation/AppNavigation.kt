package com.example.runnity.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 앱 네비게이션 구조
 * - 하단 네비게이션 (4개: 홈, 개인, 챌린지, 마이페이지)
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorPalette.Light.background,
                contentColor = ColorPalette.Light.primary
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = Typography.CaptionSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorPalette.Light.primary,
                            selectedTextColor = ColorPalette.Light.primary,
                            unselectedIconColor = ColorPalette.Light.component,
                            unselectedTextColor = ColorPalette.Light.component,
                            indicatorColor = ColorPalette.Light.containerBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 홈 화면
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }

            // 개인 러닝 화면
            composable(BottomNavItem.Individual.route) {
                IndividualScreen()
            }

            // 챌린지 화면
            composable(BottomNavItem.Challenge.route) {
                ChallengeScreen()
            }

            // 마이페이지 화면
            composable(BottomNavItem.MyPage.route) {
                MyPageScreen()
            }
        }
    }
}

/**
 * 하단 네비게이션 아이템 정의
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // TODO: 적절한 아이콘으로 교체 필요
    object Home : BottomNavItem(
        route = "home",
        label = "홈",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Individual : BottomNavItem(
        route = "individual",
        label = "개인",
        selectedIcon = Icons.Filled.Person,  // TODO: 러닝 아이콘으로 교체
        unselectedIcon = Icons.Outlined.Person
    )

    object Challenge : BottomNavItem(
        route = "challenge",
        label = "챌린지",
        selectedIcon = Icons.Filled.Person,  // TODO: 트로피 아이콘으로 교체
        unselectedIcon = Icons.Outlined.Person
    )

    object MyPage : BottomNavItem(
        route = "mypage",
        label = "마이페이지",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

// 하단 네비 아이템 리스트
private val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Individual,
    BottomNavItem.Challenge,
    BottomNavItem.MyPage
)

// ==================== 임시 화면들 ====================
// TODO: 각 화면의 실제 구현으로 교체 필요

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "홈 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}

@Composable
fun IndividualScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "개인 러닝 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}

@Composable
fun ChallengeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "챌린지 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}

@Composable
fun MyPageScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "마이페이지 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}
