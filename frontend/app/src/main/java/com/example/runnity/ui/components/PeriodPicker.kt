package com.example.runnity.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 기간 선택 바텀 시트 내용 (월 전용)
 * 년도와 월을 각각 스피너로 선택
 */
@Composable
fun MonthPeriodPicker(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    maxMonths: Int = 60  // 최대 개월 수 (기본 5년 = 60개월)
) {
    val today = java.time.LocalDate.now()

    // selectedIndex를 기반으로 초기 년월 계산
    val monthsBack = (maxMonths - 1) - selectedIndex
    val initialDate = today.minusMonths(monthsBack.toLong())

    var selectedYear by remember(selectedIndex) { mutableStateOf(initialDate.year) }
    var selectedMonth by remember(selectedIndex) { mutableStateOf(initialDate.monthValue) }

    // selectedIndex가 변경되면 선택된 년월도 업데이트
    LaunchedEffect(selectedIndex) {
        val monthsBack = (maxMonths - 1) - selectedIndex
        val targetDate = today.minusMonths(monthsBack.toLong())
        selectedYear = targetDate.year
        selectedMonth = targetDate.monthValue
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 년도 스피너
        YearSpinnerComponent(
            selectedYear = selectedYear,
            onYearSelected = { year ->
                selectedYear = year
                // 년월 변경 시 인덱스 업데이트
                val selected = java.time.LocalDate.of(year, selectedMonth, 1)
                val current = java.time.LocalDate.of(today.year, today.month, 1)
                val monthsDiff = java.time.temporal.ChronoUnit.MONTHS.between(selected, current).toInt()
                val actualIndex = if (monthsDiff in 0 until maxMonths) (maxMonths - 1) - monthsDiff else (maxMonths - 1)
                onSelected(actualIndex)
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 월 스피너
        MonthSpinnerComponent(
            selectedMonth = selectedMonth,
            onMonthSelected = { month ->
                selectedMonth = month
                // 년월 변경 시 인덱스 업데이트
                val selected = java.time.LocalDate.of(selectedYear, month, 1)
                val current = java.time.LocalDate.of(today.year, today.month, 1)
                val monthsDiff = java.time.temporal.ChronoUnit.MONTHS.between(selected, current).toInt()
                val actualIndex = if (monthsDiff in 0 until maxMonths) (maxMonths - 1) - monthsDiff else (maxMonths - 1)
                onSelected(actualIndex)
            }
        )
    }
}

/**
 * 단일 스피너 Picker (주, 연, 전체 등)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SingleSpinnerPicker(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

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

            if (centerIndex in options.indices && centerIndex != selectedIndex) {
                onSelected(centerIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // 선택 영역 표시 (중앙 구분선)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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

        // 스피너 리스트
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 75.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(options.size) { index ->
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
                        text = options[index],
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
 * 년도 스피너 컴포넌트
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearSpinnerComponent(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val years = remember { (currentYear - 4..currentYear).toList() }
    val selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            if (centerIndex in years.indices) {
                onYearSelected(years[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(200.dp)
            .width(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
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

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 75.dp)
        ) {
            items(years.size) { index ->
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
                        text = years[index].toString(),
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
 * 월 스피너 컴포넌트
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthSpinnerComponent(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    val months = remember { (1..12).toList() }
    val selectedIndex = months.indexOf(selectedMonth).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset

            val centerIndex = if (firstVisibleOffset > 25) {
                firstVisibleIndex + 1
            } else {
                firstVisibleIndex
            }

            if (centerIndex in months.indices) {
                onMonthSelected(months[centerIndex])
            }
        }
    }

    Box(
        modifier = Modifier
            .height(200.dp)
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
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

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 75.dp)
        ) {
            items(months.size) { index ->
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
                        text = String.format("%02d", months[index]),
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
