package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.*
import timber.log.Timber
import java.time.LocalDate

/**
 * 챌린지 필터 화면
 * - 거리, 날짜/시간, 공개 여부 필터링
 * - 필터 조건 선택 후 적용
 *
 * @param navController 네비게이션 컨트롤러
 * @param viewModel 챌린지 ViewModel (부모 화면과 공유)
 */
@Composable
fun ChallengeFilterScreen(
    navController: NavController? = null,
    viewModel: ChallengeViewModel
) {
    // ViewModel에서 현재 필터 값 가져오기
    val currentDistances by viewModel.distanceFilter.collectAsState()
    val currentStartDate by viewModel.startDateFilter.collectAsState()
    val currentEndDate by viewModel.endDateFilter.collectAsState()
    val currentStartTime by viewModel.startTimeFilter.collectAsState()
    val currentEndTime by viewModel.endTimeFilter.collectAsState()
    val currentVisibility by viewModel.visibilityFilter.collectAsState()

    // 거리 선택 상태 (여러 개 선택 가능)
    var selectedDistances by remember {
        mutableStateOf(
            currentDistances?.mapNotNull { code -> convertCodeToDistance(code) }?.toSet() ?: setOf()
        )
    }

    // 날짜 선택 상태 (범위)
    var selectedStartDate by remember {
        mutableStateOf<LocalDate?>(
            currentStartDate?.let { parseDate(it) }
        )
    }
    var selectedEndDate by remember {
        mutableStateOf<LocalDate?>(
            currentEndDate?.let { parseDate(it) }
        )
    }

    // 시간 선택 상태 (HH:mm:ss → HH:mm 변환)
    var selectedStartTime by remember {
        mutableStateOf(currentStartTime?.let { parseTime(it) } ?: "00:00")
    }
    var selectedEndTime by remember {
        mutableStateOf(currentEndTime?.let { parseTime(it) } ?: "24:00")
    }

    var selectedVisibility by remember {
        mutableStateOf(
            when (currentVisibility) {
                "PUBLIC" -> "공개만"
                "ALL" -> "전체"
                else -> "공개만"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 상단 헤더 (뒤로가기 + 제목 + 초기화)
        ActionHeader(
            title = "챌린지 필터",
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
                            // 초기화 (UI + ViewModel)
                            selectedDistances = setOf()
                            selectedStartDate = null
                            selectedEndDate = null
                            selectedStartTime = "00:00"
                            selectedEndTime = "24:00"
                            selectedVisibility = "공개만"

                            viewModel.applyFilters(
                                distances = null,
                                startDate = null,
                                endDate = null,
                                startTime = null,
                                endTime = null,
                                visibility = null
                            )
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

            // ===== 날짜 섹션 =====
            SectionHeader(subtitle = "날짜")

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜 선택 컴포넌트
            DateRangeSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedStartDate = selectedStartDate,
                selectedEndDate = selectedEndDate,
                onDateRangeSelected = { startDate, endDate ->
                    selectedStartDate = startDate
                    selectedEndDate = endDate
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 시간대 섹션 =====
            SectionHeader(subtitle = "시간대")

            Spacer(modifier = Modifier.height(12.dp))

            // 시간대 선택 컴포넌트
            TimeRangeSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
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
                // 거리 필터 변환 (UI -> API)
                // 여러 개 선택된 거리들을 API 코드로 변환
                val distanceCodes = if (selectedDistances.isNotEmpty()) {
                    selectedDistances.map { convertDistanceToCode(it) }
                } else {
                    null  // 선택 안하면 null (전체)
                }

                // 날짜 필터 변환 (LocalDate -> String)
                val startDate = selectedStartDate?.toString() // 2025-11-14
                val endDate = selectedEndDate?.toString()     // 2025-11-17

                // 시간 필터 변환 (HH:mm -> HH:mm:ss)
                val startTime = if (selectedStartDate != null) {
                    convertTimeToApiFormat(selectedStartTime) // 09:00:00
                } else {
                    null
                }
                val endTime = if (selectedEndDate != null) {
                    convertTimeToApiFormat(selectedEndTime)   // 23:00:00
                } else {
                    null
                }

                // 공개 여부 변환
                val visibility = when (selectedVisibility) {
                    "공개만" -> "PUBLIC"
                    "전체" -> "ALL"
                    else -> "ALL"
                }

                Timber.d("필터 적용: distances=$distanceCodes, startDate=$startDate, endDate=$endDate, startTime=$startTime, endTime=$endTime, visibility=$visibility")

                // ViewModel에 필터 적용 (상태 저장 + API 호출)
                viewModel.applyFilters(
                    distances = distanceCodes,
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    visibility = visibility
                )

                // 필터 화면 닫기
                navController?.navigateUp()
            }
        )
    }
}

/**
 * UI 거리 값을 API 거리 코드로 변환
 */
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
        else -> "FIVE"
    }
}

/**
 * API 거리 코드를 UI 거리 값으로 변환 (역변환)
 */
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
        else -> null
    }
}

/**
 * Date 문자열을 LocalDate로 파싱
 * 예: "2025-11-14" -> LocalDate
 */
private fun parseDate(dateString: String): LocalDate? {
    return try {
        LocalDate.parse(dateString)
    } catch (e: Exception) {
        Timber.w(e, "Date 파싱 실패: $dateString")
        null
    }
}

/**
 * Time 문자열을 UI 형식으로 파싱
 * 예: "09:00:00" -> "09:00"
 *     "23:59:59" -> "24:00" (특수 처리)
 */
private fun parseTime(timeString: String): String {
    return try {
        val parts = timeString.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val second = parts.getOrNull(2)?.toIntOrNull() ?: 0

        // 23:59:59는 "24:00"으로 표시 (UI 표시용)
        if (hour == 23 && minute == 59 && second == 59) {
            "24:00"
        } else {
            String.format("%02d:%02d", hour, minute)
        }
    } catch (e: Exception) {
        Timber.w(e, "Time 파싱 실패: $timeString")
        "00:00"
    }
}

/**
 * UI 시간 형식을 API 형식으로 변환
 * 예: "09:00" -> "09:00:00"
 *     "24:00" -> "23:59:59" (특수 처리)
 */
private fun convertTimeToApiFormat(time: String): String {
    val parts = time.split(":")
    var hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    var minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    var second = 0

    // "24:00" 특수 처리 -> 23:59:59
    if (hour == 24 && minute == 0) {
        hour = 23
        minute = 59
        second = 59
    }

    return String.format("%02d:%02d:%02d", hour, minute, second)
}
