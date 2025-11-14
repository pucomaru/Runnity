package com.example.runnity.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
 * 단일 날짜·시간 선택 컴포넌트 (챌린지 생성용)
 * - 오늘부터 7일간의 날짜 표시
 * - 단일 날짜 선택
 * - 시, 분, AM/PM 선택
 *
 * @param selectedDate 선택된 날짜
 * @param selectedHour 선택된 시 (1~12)
 * @param selectedMinute 선택된 분 (0~59)
 * @param selectedAmPm 선택된 AM/PM
 * @param onDateSelected 날짜 선택 이벤트
 * @param onTimeSelected 시간 선택 이벤트 (hour, minute, amPm)
 * @param modifier Modifier (선택사항)
 */
@Composable
fun SingleDateTimeSelector(
    selectedDate: LocalDate?,
    selectedHour: Int,
    selectedMinute: Int,
    selectedAmPm: String,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (Int, Int, String) -> Unit,
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
        // ===== 날짜 선택 (단일 선택) =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dateList.forEach { dateInfo ->
                val isSelected = selectedDate == dateInfo.date

                DateItem(
                    dateInfo = dateInfo,
                    isSelected = isSelected,
                    onClick = {
                        onDateSelected(dateInfo.date)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ===== 시간 선택 (시, 분, AM/PM) =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 시 스피너 (1~12)
            HourSpinnerComponent(
                selectedHour = selectedHour,
                onHourSelected = { hour ->
                    onTimeSelected(hour, selectedMinute, selectedAmPm)
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = ":",
                style = Typography.Title,
                color = ColorPalette.Light.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 분 스피너 (00~59)
            MinuteSpinnerComponent(
                selectedMinute = selectedMinute,
                onMinuteSelected = { minute ->
                    // 분은 AM/PM에 영향을 주지 않음
                    onTimeSelected(selectedHour, minute, selectedAmPm)
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // AM/PM 스피너
            AmPmSpinnerComponent(
                selectedAmPm = selectedAmPm,
                onAmPmSelected = { amPm ->
                    onTimeSelected(selectedHour, selectedMinute, amPm)
                }
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
 * 시 스피너 컴포넌트 (1~12, 무한 스크롤)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HourSpinnerComponent(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    // 무한 스크롤을 위한 매우 큰 리스트
    val totalItems = 10000
    val startOffset = totalItems / 2

    // LazyColumn 상태
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startOffset + (selectedHour % 12)
    )

    // 스크롤 멈췄을 때 중앙 아이템 선택
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            // 실제 시간 계산 (1~12 반복)
            val actualHour = ((centerIndex - startOffset) % 12).let {
                if (it <= 0) it + 12 else it
            }

            if (actualHour != selectedHour) {
                onHourSelected(actualHour)
            }
        }
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .width(60.dp),
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

        // 시간 리스트 (무한 스크롤)
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(totalItems) { index ->
                // 실제 시간 계산 (1~12 반복)
                val hour = ((index - startOffset) % 12).let {
                    if (it <= 0) it + 12 else it
                }

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
                        text = hour.toString(),
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

/**
 * 분 스피너 컴포넌트 (00~59, 무한 스크롤)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MinuteSpinnerComponent(
    selectedMinute: Int,
    onMinuteSelected: (Int) -> Unit
) {
    // 무한 스크롤을 위한 매우 큰 리스트
    val totalItems = 10000
    val startOffset = totalItems / 2

    // LazyColumn 상태
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startOffset + selectedMinute
    )

    // 스크롤 멈췄을 때 중앙 아이템 선택
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            // 실제 분 계산 (0~59 반복)
            val actualMinute = ((centerIndex - startOffset) % 60).let {
                if (it < 0) it + 60 else it
            }

            if (actualMinute != selectedMinute) {
                onMinuteSelected(actualMinute)
            }
        }
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .width(60.dp),
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

        // 분 리스트 (무한 스크롤)
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(totalItems) { index ->
                // 실제 분 계산 (0~59 반복)
                val minute = ((index - startOffset) % 60).let {
                    if (it < 0) it + 60 else it
                }

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
                        text = String.format("%02d", minute),
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

/**
 * AM/PM 스피너 컴포넌트
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AmPmSpinnerComponent(
    selectedAmPm: String,
    onAmPmSelected: (String) -> Unit
) {
    // AM/PM 리스트
    val amPmList = remember { listOf("am", "pm") }

    // 현재 선택된 AM/PM의 인덱스
    val selectedIndex = if (selectedAmPm.lowercase() == "am") 0 else 1

    // LazyColumn 상태
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    // selectedAmPm이 외부에서 변경되었을 때 스크롤 위치 업데이트
    LaunchedEffect(selectedAmPm) {
        val newIndex = if (selectedAmPm.lowercase() == "am") 0 else 1
        if (newIndex != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(newIndex)
        }
    }

    // 스크롤 멈췄을 때 중앙 아이템 선택
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            if (centerIndex in amPmList.indices) {
                onAmPmSelected(amPmList[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .width(60.dp),
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

        // AM/PM 리스트
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(amPmList.size) { index ->
                val amPm = amPmList[index]

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
                        text = amPm,
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
