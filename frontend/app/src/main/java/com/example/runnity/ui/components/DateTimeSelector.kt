package com.example.runnity.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Locale

/**
 * 날짜 정보 데이터 클래스
 */
data class DateInfo(
    val date: LocalDate,
    val dayOfWeek: String,  // "Thu", "Fri" 등
    val dayOfMonth: String, // "06", "07" 등
    val isToday: Boolean,
    val isSaturday: Boolean,
    val isSunday: Boolean
)

/**
 * 날짜 범위 선택 컴포넌트 (날짜만)
 * - 오늘부터 7일간의 날짜 표시
 * - 날짜 범위 선택 (시작 날짜 ~ 종료 날짜)
 *
 * @param selectedStartDate 선택된 시작 날짜
 * @param selectedEndDate 선택된 종료 날짜
 * @param onDateRangeSelected 날짜 범위 선택 이벤트
 * @param modifier Modifier (선택사항)
 */
@Composable
fun DateRangeSelector(
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateRangeSelected: (LocalDate?, LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 오늘부터 7일간의 날짜 생성
    val dateList = remember {
        val today = LocalDate.now()
        (0..6).map { offset ->
            val date = today.plusDays(offset.toLong())
            DateInfo(
                date = date,
                dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                dayOfMonth = date.format(DateTimeFormatter.ofPattern("dd")),
                isToday = offset == 0,
                isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY,
                isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
            )
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dateList.forEach { dateInfo ->
            val isInRange = if (selectedStartDate != null && selectedEndDate != null) {
                dateInfo.date >= selectedStartDate && dateInfo.date <= selectedEndDate
            } else {
                selectedStartDate == dateInfo.date || selectedEndDate == dateInfo.date
            }

            DateItem(
                dateInfo = dateInfo,
                isSelected = isInRange,
                onClick = {
                    // 날짜 범위 선택 로직
                    when {
                        selectedStartDate == null -> {
                            // 시작 날짜 설정
                            onDateRangeSelected(dateInfo.date, null)
                        }
                        selectedEndDate == null -> {
                            // 같은 날짜 클릭 시 선택 해제
                            if (dateInfo.date == selectedStartDate) {
                                onDateRangeSelected(null, null)
                            } else if (dateInfo.date >= selectedStartDate) {
                                // 종료 날짜 설정
                                onDateRangeSelected(selectedStartDate, dateInfo.date)
                            } else {
                                // 시작 날짜보다 이전이면 시작 날짜를 다시 설정
                                onDateRangeSelected(dateInfo.date, null)
                            }
                        }
                        else -> {
                            // 이미 범위가 선택되어 있으면 초기화하고 새로 시작
                            onDateRangeSelected(dateInfo.date, null)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 시간대 선택 컴포넌트 (시간만)
 * - 시간 범위 선택 (시작 시간 ~ 종료 시간)
 *
 * @param selectedStartTime 선택된 시작 시간
 * @param selectedEndTime 선택된 종료 시간
 * @param onTimeSelected 시간 선택 이벤트 (startTime, endTime)
 * @param modifier Modifier (선택사항)
 */
@Composable
fun TimeRangeSelector(
    selectedStartTime: String,
    selectedEndTime: String,
    onTimeSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시작 시간 스피너 (00:00 ~ 23:00)
        TimeSpinnerComponent(
            selectedTime = selectedStartTime,
            onTimeSelected = { time ->
                // 시작 시간이 종료 시간보다 크거나 같으면 종료 시간 자동 조정
                val startHour = time.substringBefore(":").toInt()
                val endHour = selectedEndTime.substringBefore(":").toInt()

                if (startHour >= endHour) {
                    // 종료 시간을 시작 시간 + 1로 조정
                    val newEndHour = startHour + 1
                    if (newEndHour <= 24) {
                        onTimeSelected(time, String.format("%02d:00", newEndHour))
                    } else {
                        // 24를 넘을 수 없으므로 시작 시간을 23으로 고정
                        onTimeSelected("23:00", "24:00")
                    }
                } else {
                    onTimeSelected(time, selectedEndTime)
                }
            },
            isStartTime = true
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 구분선 (-)
        Text(
            text = "-",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 종료 시간 스피너 (01:00 ~ 24:00)
        TimeSpinnerComponent(
            selectedTime = selectedEndTime,
            onTimeSelected = { time ->
                // 종료 시간이 시작 시간보다 작거나 같으면 시작 시간 자동 조정
                val startHour = selectedStartTime.substringBefore(":").toInt()
                val endHour = time.substringBefore(":").toInt()

                if (endHour <= startHour) {
                    // 시작 시간을 종료 시간 - 1로 조정
                    val newStartHour = endHour - 1
                    if (newStartHour >= 0) {
                        onTimeSelected(String.format("%02d:00", newStartHour), time)
                    } else {
                        // 0보다 작아질 수 없으므로 시작 0, 종료 1로 고정
                        onTimeSelected("00:00", "01:00")
                    }
                } else {
                    onTimeSelected(selectedStartTime, time)
                }
            },
            isStartTime = false
        )
    }
}

/**
 * 날짜·시간 선택 컴포넌트 (통합 버전 - 하위 호환성)
 * - 오늘부터 7일간의 날짜 표시
 * - 날짜 범위 선택 (시작 날짜 ~ 종료 날짜)
 * - 시간 범위 선택 (시작 시간 ~ 종료 시간)
 *
 * @param selectedStartDate 선택된 시작 날짜
 * @param selectedEndDate 선택된 종료 날짜
 * @param onDateRangeSelected 날짜 범위 선택 이벤트
 * @param selectedStartTime 선택된 시작 시간
 * @param selectedEndTime 선택된 종료 시간
 * @param onTimeSelected 시간 선택 이벤트 (startTime, endTime)
 * @param modifier Modifier (선택사항)
 */
@Composable
fun DateTimeSelector(
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateRangeSelected: (LocalDate?, LocalDate?) -> Unit,
    selectedStartTime: String,
    selectedEndTime: String,
    onTimeSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 오늘부터 7일간의 날짜 생성
    val dateList = remember {
        val today = LocalDate.now()
        (0..6).map { offset ->
            val date = today.plusDays(offset.toLong())
            DateInfo(
                date = date,
                dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                dayOfMonth = date.format(DateTimeFormatter.ofPattern("dd")),
                isToday = offset == 0,
                isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY,
                isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
            )
        }
    }

    Column(modifier = modifier) {
        // ===== 날짜 선택 (범위 선택) =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dateList.forEach { dateInfo ->
                val isInRange = if (selectedStartDate != null && selectedEndDate != null) {
                    dateInfo.date >= selectedStartDate && dateInfo.date <= selectedEndDate
                } else {
                    selectedStartDate == dateInfo.date || selectedEndDate == dateInfo.date
                }

                DateItem(
                    dateInfo = dateInfo,
                    isSelected = isInRange,
                    onClick = {
                        // 날짜 범위 선택 로직
                        when {
                            selectedStartDate == null -> {
                                // 시작 날짜 설정
                                onDateRangeSelected(dateInfo.date, null)
                            }
                            selectedEndDate == null -> {
                                // 같은 날짜 클릭 시 선택 해제
                                if (dateInfo.date == selectedStartDate) {
                                    onDateRangeSelected(null, null)
                                } else if (dateInfo.date >= selectedStartDate) {
                                    // 종료 날짜 설정
                                    onDateRangeSelected(selectedStartDate, dateInfo.date)
                                } else {
                                    // 시작 날짜보다 이전이면 시작 날짜를 다시 설정
                                    onDateRangeSelected(dateInfo.date, null)
                                }
                            }
                            else -> {
                                // 이미 범위가 선택되어 있으면 초기화하고 새로 시작
                                onDateRangeSelected(dateInfo.date, null)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ===== 시간 선택 =====
        // 시작 시간과 종료 시간 스피너
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 시작 시간 스피너 (00:00 ~ 23:00)
            TimeSpinnerComponent(
                selectedTime = selectedStartTime,
                onTimeSelected = { time ->
                    // 시작 시간이 종료 시간보다 크거나 같으면 종료 시간 자동 조정
                    val startHour = time.substringBefore(":").toInt()
                    val endHour = selectedEndTime.substringBefore(":").toInt()

                    if (startHour >= endHour) {
                        // 종료 시간을 시작 시간 + 1로 조정
                        val newEndHour = startHour + 1
                        if (newEndHour <= 24) {
                            onTimeSelected(time, String.format("%02d:00", newEndHour))
                        } else {
                            // 24를 넘을 수 없으므로 시작 시간을 23으로 고정
                            onTimeSelected("23:00", "24:00")
                        }
                    } else {
                        onTimeSelected(time, selectedEndTime)
                    }
                },
                isStartTime = true
            )

            Spacer(modifier = Modifier.width(24.dp))

            // 구분선 (-)
            Text(
                text = "-",
                style = Typography.Title,
                color = ColorPalette.Light.primary
            )

            Spacer(modifier = Modifier.width(24.dp))

            // 종료 시간 스피너 (01:00 ~ 24:00)
            TimeSpinnerComponent(
                selectedTime = selectedEndTime,
                onTimeSelected = { time ->
                    // 종료 시간이 시작 시간보다 작거나 같으면 시작 시간 자동 조정
                    val startHour = selectedStartTime.substringBefore(":").toInt()
                    val endHour = time.substringBefore(":").toInt()

                    if (endHour <= startHour) {
                        // 시작 시간을 종료 시간 - 1로 조정
                        val newStartHour = endHour - 1
                        if (newStartHour >= 0) {
                            onTimeSelected(String.format("%02d:00", newStartHour), time)
                        } else {
                            // 0보다 작아질 수 없으므로 시작 0, 종료 1로 고정
                            onTimeSelected("00:00", "01:00")
                        }
                    } else {
                        onTimeSelected(selectedStartTime, time)
                    }
                },
                isStartTime = false
            )
        }
    }
}

/**
 * 날짜 아이템 컴포넌트
 * - 요일과 날짜 표시
 * - 오늘 날짜는 테두리 표시
 * - 토요일은 파란색, 일요일은 빨간색
 */
@Composable
private fun DateItem(
    dateInfo: DateInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 요일 색상
    val dayColor = when {
        dateInfo.isSaturday -> Color(0xFF2196F3) // 파란색
        dateInfo.isSunday -> Color(0xFFF44336)   // 빨간색
        else -> ColorPalette.Light.secondary     // 회색
    }

    // 날짜 색상 (선택 여부에 따라)
    val dateColor = if (isSelected) {
        Color.White
    } else {
        ColorPalette.Light.primary
    }

    // 배경 색상
    val backgroundColor = if (isSelected) {
        ColorPalette.Common.accent
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 요일 (Caption)
        Text(
            text = dateInfo.dayOfWeek,
            style = Typography.Caption,
            color = dayColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 날짜 (Subheading)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = backgroundColor,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateInfo.dayOfMonth,
                style = Typography.Subheading,
                color = dateColor,
                textAlign = TextAlign.Center
            )
        }

        // 오늘 날짜 표시 (작은 점)
        if (dateInfo.isToday) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = ColorPalette.Common.accent,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

/**
 * 시간 스피너 컴포넌트
 * - 시작 시간: 00:00 ~ 23:00
 * - 종료 시간: 01:00 ~ 24:00
 * - 스크롤하여 시간 선택
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeSpinnerComponent(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    isStartTime: Boolean
) {
    // 시간 리스트 생성
    val timeList = remember(isStartTime) {
        if (isStartTime) {
            // 시작 시간: 00:00 ~ 23:00
            (0..23).map { hour ->
                String.format("%02d:00", hour)
            }
        } else {
            // 종료 시간: 01:00 ~ 24:00
            (1..24).map { hour ->
                String.format("%02d:00", hour)
            }
        }
    }

    // 현재 선택된 시간의 인덱스
    val selectedIndex = timeList.indexOf(selectedTime).coerceAtLeast(0)

    // LazyColumn 상태
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val coroutineScope = rememberCoroutineScope()

    // selectedTime이 외부에서 변경되었을 때 스크롤 위치 업데이트 (초기화 버튼 등)
    LaunchedEffect(selectedTime) {
        val newIndex = timeList.indexOf(selectedTime)
        if (newIndex >= 0 && newIndex != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(newIndex)
        }
    }

    // 스크롤 멈췄을 때 중앙 아이템 선택
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            // 중앙에 가장 가까운 아이템 계산
            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            if (centerIndex in timeList.indices) {
                onTimeSelected(timeList[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // 선택 영역 표시 (중앙 구분선)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = ColorPalette.Light.component,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(50.dp))
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = ColorPalette.Light.component,
                thickness = 1.dp
            )
        }

        // 시간 리스트
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(timeList.size) { index ->
                val time = timeList[index]

                // 중앙에 정확히 위치한 아이템만 선택된 것으로 표시
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val firstVisibleOffset = listState.firstVisibleItemScrollOffset
                val centerIndex = if (firstVisibleOffset > 25) {
                    firstVisibleIndex + 1
                } else {
                    firstVisibleIndex
                }
                val isSelected = centerIndex == index

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        style = Typography.Title,
                        color = if (isSelected) {
                            ColorPalette.Light.primary
                        } else {
                            ColorPalette.Light.component.copy(alpha = 0.4f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
