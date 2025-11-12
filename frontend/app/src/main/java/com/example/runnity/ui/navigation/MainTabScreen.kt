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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.screens.home.HomeScreen
import com.example.runnity.ui.screens.startrun.StartRunScreen
import com.example.runnity.ui.screens.challenge.ChallengeScreen
import com.example.runnity.ui.screens.challenge.ChallengeDetailScreen
import com.example.runnity.ui.screens.challenge.ChallengeFilterScreen
import com.example.runnity.ui.screens.challenge.ChallengeCreateScreen
import com.example.runnity.ui.screens.mypage.MyPageScreen
import com.example.runnity.ui.screens.mypage.ProfileSettingScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.runnity.ui.screens.broadcast.BroadcastScreen
import com.example.runnity.ui.screens.workout.WorkoutPersonalScreen
import com.example.runnity.ui.screens.workout.CountdownScreen

/**
 * 메인 탭 화면
 * - 하단 네비게이션 바 포함 (조건부 표시)
 * - 4개 탭: 홈, 개인(러닝 시작), 챌린지, 마이페이지
 * - Nested Navigation 구조 (각 탭이 여러 화면을 가질 수 있음)
 *
 * 구조:
 * - 홈 그래프: 홈 화면 + 챌린지 세부 화면
 * - 챌린지 그래프: 챌린지 리스트 화면 + 챌린지 세부 화면
 * - 세부 화면에서는 하단 네비바 숨김
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    parentNavController: NavController? = null  // 앱 전체 네비게이션 (로그인, 러닝 화면 등으로 이동 시 사용)
) {
    // 탭 네비게이션용 NavController (하단 탭 4개 + 각 탭의 세부 화면들 관리)
    val navController = rememberNavController()

    // 현재 어느 화면에 있는지 관찰 (by는 코틀린 위임 프로퍼티)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 현재 화면의 route 가져오기 (예: "home", "challenge", "challenge_detail/123")
    val currentRoute = currentDestination?.route

    // 하단 네비바를 보여줄 화면들 정의 (리스트 화면들만)
    // 세부 화면(challenge_detail)에서는 네비바를 숨김
    val showBottomBar = currentRoute in listOf(
        "home",              // 홈 화면
        "start_run",         // 개인 러닝 화면
        "challenge",         // 챌린지 리스트 화면
        "challenge_filter",  // 챌린지 필터 화면
        "challenge_create",  // 챌린지 생성 화면
        "mypage"             // 마이페이지 화면
    )

    // Scaffold: 상단바, 하단바, 플로팅 버튼 등을 배치하는 레이아웃
    Scaffold(
        bottomBar = {
            // showBottomBar가 true일 때만 하단 네비바 표시
            if (showBottomBar) {
                // Ripple 효과 비활성화 (물결 애니메이션 제거)
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    NavigationBar(
                        containerColor = ColorPalette.Light.background,
                        contentColor = ColorPalette.Light.primary
                    ) {
                        // bottomNavItems: [Home, StartRun, Challenge, MyPage]
                        bottomNavItems.forEach { item ->
                            // 현재 이 탭이 선택됐는지 확인
                            // hierarchy: 부모 그래프까지 포함해서 확인 (home_graph 안의 challenge_detail도 Home 탭으로 인식)
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.graphRoute  // graphRoute로 비교 (home_graph, challenge_graph 등)
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    // 탭 클릭 시 해당 탭의 시작 화면으로 이동
                                    navController.navigate(item.route) {
                                        // 시작 지점까지 백스택 비우기 (중복 방지)
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true  // 이전 상태 저장 (세부 화면 보다가 다른 탭 갔다가 돌아오면 세부 화면 그대로)
                                        }
                                        launchSingleTop = true  // 같은 화면 중복 생성 방지
                                        restoreState = true      // 저장된 상태 복원
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
        }
    ) { innerPadding ->
        // NavHost: 화면 전환을 관리하는 컨테이너
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.graphRoute,  // 시작 화면: home_graph
            modifier = Modifier.padding(innerPadding)
        ) {
            // ========== 홈 그래프 ==========
            // navigation(): 여러 화면을 하나의 그룹으로 묶음
            navigation(
                startDestination = BottomNavItem.Home.route,    // 이 그래프의 시작: "home"
                route = BottomNavItem.Home.graphRoute           // 그래프 이름: "home_graph"
            ) {
                // 홈 화면 (예약한 챌린지 리스트)
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        navController = navController,           // 탭 내부 이동용 (세부 화면으로)
                        parentNavController = parentNavController // 앱 전체 이동용 (로그인 등)
                    )
                }

                // 챌린지 세부 화면 (네비바 없음)
                // {id}는 파라미터 (예: challenge_detail/123)
                composable("challenge_detail/{id}") { backStackEntry ->
                    val challengeId = backStackEntry.arguments?.getString("id") ?: ""
                    ChallengeDetailScreen(
                        challengeId = challengeId,
                        navController = navController  // 뒤로가기용
                    )
                }
            }

            // ========== 개인 러닝 그래프 ==========
            navigation(
                startDestination = BottomNavItem.StartRun.route,
                route = BottomNavItem.StartRun.graphRoute
            ) {
                composable(BottomNavItem.StartRun.route) {
                    StartRunScreen(
                        navController = navController,
                        parentNavController = parentNavController
                    )
                }
                // 카운트다운 화면 (개인)
                composable(
                    route = "countdown/personal?type={type}&km={km}&min={min}",
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("km") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("min") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")
                    val km = backStackEntry.arguments?.getString("km")
                    val min = backStackEntry.arguments?.getString("min")
                    CountdownScreen(
                        navController = navController,
                        type = type,
                        km = km,
                        min = min
                    )
                }
                // 운동 화면 (개인)
                composable(
                    route = "workout/personal?type={type}&km={km}&min={min}",
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("km") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("min") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")
                    val km = backStackEntry.arguments?.getString("km")
                    val min = backStackEntry.arguments?.getString("min")
                    WorkoutPersonalScreen(type = type, km = km, min = min, navController = navController)
                }
                composable(
                    route = "workout/result?type={type}&km={km}&min={min}",
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("km") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("min") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type")
                    val km = backStackEntry.arguments?.getString("km")
                    val min = backStackEntry.arguments?.getString("min")
                    com.example.runnity.ui.screens.workout.WorkoutResultScreen(
                        type = type,
                        km = km,
                        min = min,
                        onClose = { navController.navigate("start_run") }
                    )
                }
            }

            // ========== 챌린지 그래프 ==========
            navigation(
                startDestination = BottomNavItem.Challenge.route,
                route = BottomNavItem.Challenge.graphRoute
            ) {
                // 챌린지 리스트 화면 (전체 챌린지)
                composable(BottomNavItem.Challenge.route) {
                    ChallengeScreen(
                        navController = navController,           // 세부 화면으로 이동용
                        parentNavController = parentNavController
                    )
                }

                // 챌린지 필터 화면 (네비바 있음)
                composable("challenge_filter") {
                    ChallengeFilterScreen(
                        navController = navController  // 뒤로가기용
                    )
                }

                // 챌린지 생성 화면 (네비바 있음)
                composable("challenge_create") {
                    ChallengeCreateScreen(
                        navController = navController  // 뒤로가기용
                    )
                }

                // 챌린지 세부 화면 (네비바 없음)
                // 홈 그래프와 동일한 화면이지만, 백스택이 다름 (뒤로가기 시 각자의 리스트로)
                composable("challenge_detail/{id}") { backStackEntry ->
                    val challengeId = backStackEntry.arguments?.getString("id") ?: ""
                    ChallengeDetailScreen(
                        challengeId = challengeId,
                        navController = navController
                    )
                }

                // 챌린지 중계 화면
                composable("broadcast_view") {
                    BroadcastScreen(
                        navController = navController,
                        parentNavController = parentNavController
                    )
                }
            }

            // ========== 마이페이지 그래프 ==========
            navigation(
                startDestination = BottomNavItem.MyPage.route,
                route = BottomNavItem.MyPage.graphRoute
            ) {
                composable(BottomNavItem.MyPage.route) {
                    MyPageScreen(
                        navController = navController,
                        parentNavController = parentNavController
                    )
                }

                // 프로필 설정 화면 (네비바 없음)
                composable("profile_setting") {
                    ProfileSettingScreen(
                        navController = navController,
                        parentNavController = parentNavController
                    )
                }
            }
        }
    }
}
