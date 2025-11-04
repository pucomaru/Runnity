package com.example.runnity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.screens.home.HomeScreen
import com.example.runnity.ui.screens.startrun.StartRunScreen
import com.example.runnity.ui.screens.challenge.ChallengeScreen
import com.example.runnity.ui.screens.mypage.MyPageScreen

/**
 * 메인 탭 화면
 * - 하단 네비게이션 바 포함
 * - 4개 탭: 홈, 개인(러닝 시작), 챌린지, 마이페이지
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    parentNavController: NavController? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
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
                                    style = Typography.Caption
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorPalette.Light.primary,
                                selectedTextColor = ColorPalette.Light.primary,
                                unselectedIconColor = ColorPalette.Light.component,
                                unselectedTextColor = ColorPalette.Light.component,
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(parentNavController = parentNavController)
            }

            composable(BottomNavItem.StartRun.route) {
                StartRunScreen(parentNavController = parentNavController)
            }

            composable(BottomNavItem.Challenge.route) {
                ChallengeScreen(parentNavController = parentNavController)
            }

            composable(BottomNavItem.MyPage.route) {
                MyPageScreen(parentNavController = parentNavController)
            }
        }
    }
}
