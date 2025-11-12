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
import java.time.LocalDate

/**
 * 중계 필터 화면
 * - 거리, 날짜/시간, 공개 여부 필터링
 * - 필터 조건 선택 후 적용
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun ChallengeFilterScreen(
    navController: NavController? = null
) {
    // 거리 선택 상태 (여러 개 선택 가능)
    var selectedDistances by remember { mutableStateOf(setOf<String>()) }

    // 날짜 선택 상태 (범위)
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }

    // 시간 선택 상태
    var selectedStartTime by remember { mutableStateOf("00:00") }
    var selectedEndTime by remember { mutableStateOf("24:00") }

    // 공개 여부 선택 상태
    var selectedVisibility by remember { mutableStateOf("공개만") }

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
                            selectedStartDate = null
                            selectedEndDate = null
                            selectedStartTime = "00:00"
                            selectedEndTime = "24:00"
                            selectedVisibility = "공개만"
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

            // 세 번째 줄: 15km, 하프 (가운데 정렬)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SmallPillButton(
                    text = "15km",
                    selected = selectedDistances.contains("15km"),
                    onClick = {
                        selectedDistances = if (selectedDistances.contains("15km")) {
                            selectedDistances - "15km"
                        } else {
                            selectedDistances + "15km"
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SmallPillButton(
                    text = "하프",
                    selected = selectedDistances.contains("하프"),
                    onClick = {
                        selectedDistances = if (selectedDistances.contains("하프")) {
                            selectedDistances - "하프"
                        } else {
                            selectedDistances + "하프"
                        }
                    },
                    modifier = Modifier.width(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 날짜 · 시간 섹션 =====
            SectionHeader(subtitle = "날짜 · 시간")

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜·시간 선택 컴포넌트
            DateTimeSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedStartDate = selectedStartDate,
                selectedEndDate = selectedEndDate,
                onDateRangeSelected = { startDate, endDate ->
                    selectedStartDate = startDate
                    selectedEndDate = endDate
                },
                selectedStartTime = selectedStartTime,
                selectedEndTime = selectedEndTime,
                onTimeSelected = { startTime, endTime ->
                    selectedStartTime = startTime
                    selectedEndTime = endTime
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 공개 여부 섹션 =====
            SectionHeader(subtitle = "공개 여부")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallPillButton(
                    text = "공개만",
                    selected = selectedVisibility == "공개만",
                    onClick = { selectedVisibility = "공개만" },
                    modifier = Modifier.weight(1f)
                )
                SmallPillButton(
                    text = "전체",
                    selected = selectedVisibility == "전체",
                    onClick = { selectedVisibility = "전체" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. 하단 적용하기 버튼
        PrimaryButton(
            text = "적용하기",
            onClick = {
                // TODO: 필터 적용 로직
                // - selectedDistances, selectedDate, selectedStartTime, selectedEndTime, selectedVisibility
                // - 이 값들을 ViewModel에 전달하거나 Navigation arguments로 전달
                navController?.navigateUp()
            }
        )
    }
}
