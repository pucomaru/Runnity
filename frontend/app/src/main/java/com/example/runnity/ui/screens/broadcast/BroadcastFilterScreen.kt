package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.*

/**
 * 중계 필터 화면
 * - 거리, 날짜/시간, 공개 여부 필터링
 * - 필터 조건 선택 후 적용
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun BroadcastFilterScreen(
    navController: NavController? = null,
    viewModel: BroadcastViewModel
) {
    // 거리 선택 상태 (여러 개 선택 가능)
    val currentDistances by viewModel.distanceFilter.collectAsState()

    var selectedDistances by remember {
        mutableStateOf(
            currentDistances?.mapNotNull { code -> convertCodeToDistance(code) }?.toSet() ?: setOf()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 상단 헤더 (뒤로가기 + 제목 + 초기화)
        ActionHeader(
            title = "중계 필터",
            onBack = { navController?.navigateUp() },
            height = 56.dp,
            rightAction = {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "초기화",
                    tint = ColorPalette.Light.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            // 초기화
                            selectedDistances = setOf()
                        }
                )
            }
        )

        // 2. 스크롤 가능한 필터 컨텐츠
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ===== 거리(km) 섹션 =====
            SectionHeader(subtitle = "거리(km)")

            Spacer(modifier = Modifier.height(12.dp))

            // 첫 번째 줄: 1km ~ 5km
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("1km", "2km", "3km", "4km", "5km").forEach { distance ->
                    SmallPillButton(
                        text = distance,
                        selected = selectedDistances.contains(distance),
                        onClick = {
                            selectedDistances = if (selectedDistances.contains(distance)) {
                                selectedDistances - distance
                            } else {
                                selectedDistances + distance
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 두 번째 줄: 6km ~ 10km
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("6km", "7km", "8km", "9km", "10km").forEach { distance ->
                    SmallPillButton(
                        text = distance,
                        selected = selectedDistances.contains(distance),
                        onClick = {
                            selectedDistances = if (selectedDistances.contains(distance)) {
                                selectedDistances - distance
                            } else {
                                selectedDistances + distance
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 세 번째 줄: 15km, 하프, 100m, 500m
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.25f))
                listOf("15km", "하프", "100m", "500m").forEach { distance ->
                    SmallPillButton(
                        text = distance,
                        selected = selectedDistances.contains(distance),
                        onClick = {
                            selectedDistances = if (selectedDistances.contains(distance)) {
                                selectedDistances - distance
                            } else {
                                selectedDistances + distance
                            }
                        },
                        modifier = Modifier.weight(1.15f)
                    )
                }
                Spacer(modifier = Modifier.weight(0.25f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. 하단 적용하기 버튼
        PrimaryButton(
            text = "적용하기",
            onClick = {
                val distanceCodes = if (selectedDistances.isNotEmpty()) {
                    selectedDistances.map { convertDistanceToCode(it) }
                } else {
                    null
                }
                viewModel.applyFilters(distance = distanceCodes)
                navController?.navigateUp()
            }
        )
    }
}

private fun convertDistanceToCode(distance: String): String {
    return when (distance) {
        "1km" -> "ONE"
        "2km" -> "TWO"
        "3km" -> "THREE"
        "4km" -> "FOUR"
        "5km" -> "FIVE"
        "6km" -> "SIX"
        "7km" -> "SEVEN"
        "8km" -> "EIGHT"
        "9km" -> "NINE"
        "10km" -> "TEN"
        "15km" -> "FIFTEEN"
        "하프" -> "HALF"
        "100m" -> "M100"
        "500m" -> "M500"
        else -> "FIVE" // 기본값
    }
}

private fun convertCodeToDistance(code: String): String? {
    return when (code) {
        "ONE" -> "1km"
        "TWO" -> "2km"
        "THREE" -> "3km"
        "FOUR" -> "4km"
        "FIVE" -> "5km"
        "SIX" -> "6km"
        "SEVEN" -> "7km"
        "EIGHT" -> "8km"
        "NINE" -> "9km"
        "TEN" -> "10km"
        "FIFTEEN" -> "15km"
        "HALF" -> "하프"
        "M100" -> "100m"
        "M500" -> "500m"
        else -> null
    }
}
