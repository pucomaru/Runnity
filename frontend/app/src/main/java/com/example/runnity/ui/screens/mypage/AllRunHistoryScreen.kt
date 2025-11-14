package com.example.runnity.ui.screens.mypage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.components.YearSpinnerComponent
import com.example.runnity.ui.components.MonthSpinnerComponent
import com.example.runnity.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 운동 기록 페이지
 * - 마이페이지 -> "모든 운동 기록" 버튼 클릭 시 이동
 * - 달력을 통해 해당 월/일에 기록한 운동 기록을 표시
 * - 운동 기록 리스트에서 항목 클릭 시 개인/챌린지 운동 기록 상세로 이동
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllRunHistoryScreen(
    navController: NavController
) {
    val today = LocalDate.now()

    // 선택된 날짜
    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    // 년/월 선택 모달 상태
    var showYearMonthModal by remember { mutableStateOf(false) }

    // HorizontalPager 상태 (10년치 = 120개월)
    val pagerState = rememberPagerState(
        initialPage = 119, // 이번 달로 시작
        pageCount = { 120 }
    )

    val coroutineScope = rememberCoroutineScope()

    // 현재 페이지에 해당하는 년/월 계산
    val currentYearMonth = remember(pagerState.currentPage) {
        today.minusMonths((119 - pagerState.currentPage).toLong())
    }

    val selectedYear = currentYearMonth.year
    val selectedMonth = currentYearMonth.monthValue

    // 운동한 날짜 목록 (임시 - 나중에 API로 대체)
    val runDates = remember {
        mapOf(
            LocalDate.of(2025, 11, 5) to listOf("personal"),
            LocalDate.of(2025, 11, 10) to listOf("challenge"),
            LocalDate.of(2025, 11, 14) to listOf("personal", "challenge")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 헤더
        ActionHeader(
            title = "운동 기록",
            onBack = { navController.navigateUp() },
            height = 56.dp
        )

        // 2. 년/월 선택 버튼 (드롭다운 아이콘 제거, 중앙 정렬)
        YearMonthSelector(
            year = selectedYear,
            month = selectedMonth,
            onClick = { showYearMonthModal = true }
        )

        // 3. 스와이프 가능한 달력
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) // 고정 높이
        ) { page ->
            val pageYearMonth = today.minusMonths((119 - page).toLong())

            MonthCalendar(
                year = pageYearMonth.year,
                month = pageYearMonth.monthValue,
                selectedDate = selectedDate,
                runDates = runDates,
                onDateSelected = { date ->
                    selectedDate = date
                    Timber.d("선택된 날짜: $date")
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 선택된 날짜의 운동 기록 리스트 (배경 구분 없이)
        val currentSelectedDate = selectedDate
        if (currentSelectedDate != null && runDates.containsKey(currentSelectedDate)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 날짜 표시
                Text(
                    text = "${currentSelectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 운동 기록",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 임시 더미 데이터
                val records = remember(currentSelectedDate) {
                    listOf(
                        RunningRecord(
                            id = "1",
                            type = "personal",
                            distance = 5.2,
                            pace = "5'30\"",
                            time = "28:36",
                            date = currentSelectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        ),
                        RunningRecord(
                            id = "2",
                            type = "challenge",
                            distance = 3.0,
                            pace = "6'00\"",
                            time = "18:00",
                            date = currentSelectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                        )
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(records) { record ->
                        RunningRecordItem(
                            record = record,
                            onClick = {
                                // 운동 기록 상세 페이지로 이동
                                when (record.type) {
                                    "personal" -> navController.navigate("personal_run_detail/${record.id}")
                                    "challenge" -> navController.navigate("challenge_run_detail/${record.id}")
                                }
                            }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "운동 기록이 없습니다",
                    style = Typography.Body,
                    color = ColorPalette.Light.secondary
                )
            }
        }
    }

    // 년/월 선택 모달
    if (showYearMonthModal) {
        YearMonthPickerModal(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            onYearMonthSelected = { year, month ->
                // 선택한 년/월로 페이지 이동
                val targetYearMonth = YearMonth.of(year, month)
                val currentYearMonth = YearMonth.from(today)
                val monthsDiff = java.time.temporal.ChronoUnit.MONTHS.between(targetYearMonth, currentYearMonth).toInt()

                if (monthsDiff in 0..119) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(119 - monthsDiff)
                    }
                }

                selectedDate = null // 월 변경 시 선택 초기화
                showYearMonthModal = false
            },
            onDismiss = { showYearMonthModal = false }
        )
    }
}

/**
 * 년/월 선택 버튼 (드롭다운 아이콘 제거, 중앙 정렬)
 */
@Composable
private fun YearMonthSelector(
    year: Int,
    month: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%d.%02d", year, month),
            style = Typography.Subheading,
            color = ColorPalette.Light.primary
        )
    }
}

/**
 * 월 달력 컴포넌트
 */
@Composable
private fun MonthCalendar(
    year: Int,
    month: Int,
    selectedDate: LocalDate?,
    runDates: Map<LocalDate, List<String>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearMonth = YearMonth.of(year, month)
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // 첫 번째 날의 요일 (월요일 = 1, 일요일 = 7)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일을 0으로

    // 달력에 표시할 날짜들 생성
    val calendarDates = remember(year, month) {
        val dates = mutableListOf<LocalDate?>()

        // 앞쪽 빈 칸
        repeat(firstDayOfWeek) {
            dates.add(null)
        }

        // 실제 날짜들
        var currentDay = firstDayOfMonth
        while (currentDay <= lastDayOfMonth) {
            dates.add(currentDay)
            currentDay = currentDay.plusDays(1)
        }

        dates
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { dayOfWeek ->
                val color = when (dayOfWeek) {
                    "일" -> Color(0xFFF44336)
                    "토" -> Color(0xFF2196F3)
                    else -> ColorPalette.Light.secondary
                }

                Text(
                    text = dayOfWeek,
                    style = Typography.Caption,
                    color = color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 날짜 그리드 (7열) - 고정 높이로 주 개수에 관계없이 동일
        val weeks = remember(calendarDates) {
            calendarDates.chunked(7)
        }

        val weekCount = weeks.size
        val weekHeight = remember(weekCount) {
            // 전체 높이를 주 개수로 나눔 (요일 헤더 제외)
            (240.dp - 8.dp) / weekCount
        }

        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(weekHeight),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { date ->
                    CalendarDateItem(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        runTypes = date?.let { runDates[it] },
                        onClick = {
                            date?.let { onDateSelected(it) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 마지막 주가 7일 미만일 경우 빈 칸 추가
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 달력 날짜 아이템
 */
@Composable
private fun CalendarDateItem(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    runTypes: List<String>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (date == null) {
        // 빈 칸
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    val dayOfWeek = date.dayOfWeek
    val textColor = when {
        isSelected -> Color.White
        dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFF44336)
        dayOfWeek == DayOfWeek.SATURDAY -> Color(0xFF2196F3)
        else -> ColorPalette.Light.primary
    }

    val backgroundColor = if (isSelected) {
        ColorPalette.Common.accent
    } else {
        Color.Transparent
    }

    // 운동 기록 타입 확인 (각각 하나씩만)
    val hasPersonal = runTypes?.contains("personal") == true
    val hasChallenge = runTypes?.contains("challenge") == true

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 날짜 숫자
            Text(
                text = date.dayOfMonth.toString(),
                style = Typography.Body,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // 운동 기록 또는 오늘 표시 (둘 중 하나만)
            when {
                hasPersonal || hasChallenge -> {
                    // 운동 기록 표시 (점) - 개인 왼쪽, 챌린지 오른쪽
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(12.dp)
                    ) {
                        if (hasPersonal) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = if (isSelected) Color.White else ColorPalette.Common.accent,
                                        shape = CircleShape
                                    )
                            )
                        }

                        if (hasPersonal && hasChallenge) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        if (hasChallenge) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = if (isSelected) Color.White else ColorPalette.Common.stopAccent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                isToday && !isSelected -> {
                    // 오늘 표시 (운동 기록이 없을 때만)
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = ColorPalette.Common.accent,
                                shape = CircleShape
                            )
                    )
                }
                else -> {
                    // 빈 공간 유지 (레이아웃 일관성을 위해)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

/**
 * 년/월 선택 모달
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearMonthPickerModal(
    selectedYear: Int,
    selectedMonth: Int,
    onYearMonthSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempYear by remember { mutableStateOf(selectedYear) }
    var tempMonth by remember { mutableStateOf(selectedMonth) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "년월 선택",
                style = Typography.Subheading,
                color = ColorPalette.Light.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            // 년/월 스피너
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 년도 스피너
                YearSpinnerComponent(
                    selectedYear = tempYear,
                    onYearSelected = { year ->
                        tempYear = year
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 월 스피너
                MonthSpinnerComponent(
                    selectedMonth = tempMonth,
                    onMonthSelected = { month ->
                        tempMonth = month
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 확인 버튼 (PrimaryButton 사용)
            PrimaryButton(
                text = "확인",
                onClick = {
                    onYearMonthSelected(tempYear, tempMonth)
                }
            )
        }
    }
}

/**
 * 운동 기록 아이템
 */
@Composable
private fun RunningRecordItem(
    record: RunningRecord,
    onClick: () -> Unit = {}
) {
    val icon = if (record.type == "challenge") {
        Icons.Filled.Group
    } else {
        Icons.Filled.DirectionsRun
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = ColorPalette.Light.containerBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (record.type == "challenge") "챌린지" else "러닝",
                    tint = ColorPalette.Common.accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 정보 (중간)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 거리
                Text(
                    text = "${record.distance} km",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )

                // 페이스와 시간
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "평균 페이스 ${record.pace}",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                    Text(
                        text = "시간 ${record.time}",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                }
            }
        }
    }
}
