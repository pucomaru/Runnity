package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 간단한 막대 그래프
 * @param data 그래프 데이터 포인트 리스트
 * @param modifier 수정자
 * @param showXAxisLabels X축 레이블 표시 여부 (기본값: 모두 표시)
 * @param detailedData 상세 데이터 (캡션용)
 */
@Composable
fun SimpleBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    showXAxisLabels: Boolean = true,
    detailedData: List<BarChartDetailedData>? = null
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    // 선택된 막대 인덱스 (-1은 선택 없음)
    var selectedBarIndex by remember { mutableStateOf(-1) }

    // 막대 개수에 따라 두께 조절
    val barWidth = remember(data.size) {
        when {
            data.size <= 7 -> 24.dp    // 주간 (7개)
            data.size <= 12 -> 16.dp   // 연간 (12개)
            data.size <= 31 -> 8.dp    // 월간 (28-31개)
            else -> 6.dp               // 전체 (연도별, 많을 경우)
        }
    }

    // Y축 값 계산 (0부터 최댓값까지 4단계)
    val yAxisValues = remember(maxValue) {
        val step = maxValue / 4.0
        (0..4).map { (step * it).toInt().toDouble() }.reversed()
    }

    BoxWithConstraints(
        modifier = modifier,
        propagateMinConstraints = false
    ) {
        val graphWidth = maxWidth

        // 선택된 막대의 높이 계산 (캡션 Y 위치 계산용)
        val selectedBarHeight = remember(selectedBarIndex, data, maxValue) {
            if (selectedBarIndex >= 0 && selectedBarIndex < data.size) {
                val point = data[selectedBarIndex]
                if (maxValue > 0 && point.value > 0) {
                    (point.value / maxValue * 150).dp
                } else {
                    1.dp
                }
            } else {
                0.dp
            }
        }

        // 메인 그래프 Column
        Column(modifier = Modifier.fillMaxWidth()) {
            // 그래프 영역 (막대 + Y축)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) {
                // 막대 그래프
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, point ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // 막대
                            val barHeight = if (maxValue > 0) {
                                (point.value / maxValue * 150).dp
                            } else {
                                0.dp
                            }

                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .height(if (point.value > 0) barHeight else 1.dp)
                                    .background(
                                        color = if (point.value > 0) ColorPalette.Common.accent else ColorPalette.Light.containerBackground,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedBarIndex = if (selectedBarIndex == index) -1 else index
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Y축 레이블 (오른쪽)
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    yAxisValues.forEach { value ->
                        Text(
                            text = if (value == 0.0) "0" else value.toInt().toString(),
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.secondary,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // X축 레이블
            if (showXAxisLabels) {
                // 월 그래프 (많은 막대 + 적은 레이블)인지 확인
                val labels = data.filter { it.label.isNotEmpty() }
                val isMonthGraph = data.size > 20 && labels.size <= 3

                if (isMonthGraph && labels.size == 3) {
                    // 월 그래프: 레이블이 여러 칸을 차지하도록 배치
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 1일 (왼쪽)
                            Text(
                                text = labels[0].label,
                                style = Typography.CaptionSmall,
                                color = ColorPalette.Light.secondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            // 15일 (중앙)
                            Text(
                                text = labels[1].label,
                                style = Typography.CaptionSmall,
                                color = ColorPalette.Light.secondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            // 마지막 날 (오른쪽)
                            Text(
                                text = labels[2].label,
                                style = Typography.CaptionSmall,
                                color = ColorPalette.Light.secondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        Spacer(modifier = Modifier.width(48.dp)) // Y축 레이블 공간만큼 패딩
                    }
                } else {
                    // 기존 로직 (주/연/전체)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            data.forEachIndexed { index, point ->
                                // 빈 문자열이 아닌 경우에만 표시
                                if (point.label.isNotEmpty()) {
                                    Text(
                                        text = point.label,
                                        style = Typography.CaptionSmall,
                                        color = ColorPalette.Light.secondary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // 빈 문자열인 경우 공간만 차지
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(48.dp)) // Y축 레이블 공간만큼 패딩
                    }
                }
            }
        }

        // 캡션 오버레이 (공간 차지 안하고 선택된 막대 바로 위에 표시)
        if (selectedBarIndex >= 0 && detailedData != null && selectedBarIndex < detailedData.size) {
            val detailData = detailedData[selectedBarIndex]

            // 데이터가 0보다 클 때만 캡션 표시
            if (detailData.distance > 0) {
                // Y축 레이블 공간 제외한 실제 그래프 영역 비율
                val yAxisWidth = 48.dp
                val graphAreaWidth = graphWidth - yAxisWidth

                // 선택된 막대의 X 위치 계산
                val barCount = data.size
                val barSpacing = graphAreaWidth / barCount
                val xOffset = barSpacing * selectedBarIndex + (barSpacing / 2)

                // 캡션 Y 위치 계산: 그래프 영역 높이(150dp) - 막대 높이 - 캡션 높이(약 90dp)
                // 음수가 되지 않도록 최소값 설정 (위로 잘리지 않게)
                val captionHeight = 90.dp
                val captionMargin = 8.dp  // 캡션을 조금 더 위로
                val yOffset = (150.dp - selectedBarHeight - captionHeight - captionMargin).coerceAtLeast(0.dp)

                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(
                            x = xOffset - (graphWidth / 2) + (yAxisWidth / 2),
                            y = yOffset  // 선택된 막대 바로 위에 위치
                        )
                        .zIndex(10f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // 날짜/기간 표시
                        Text(
                            text = formatCaptionLabel(detailData.label),
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.component
                        )
                        Text(
                            text = String.format("%.2f km", detailData.distance),
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.primary
                        )
                        Text(
                            text = formatTime(detailData.time),
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            text = formatPace(detailData.pace),
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.secondary
                        )
                        Text(
                            text = "${detailData.count}회",
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.secondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 막대 그래프 데이터 포인트
 */
data class BarChartData(
    val label: String,
    val value: Double
)

/**
 * 막대 그래프 상세 데이터 (캡션용)
 */
data class BarChartDetailedData(
    val label: String,
    val distance: Double,
    val time: Int,      // 초 단위
    val pace: Int,      // 초 단위
    val count: Int
)

/**
 * 시간 포맷팅 (초 -> H:MM:SS)
 */
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%d:%02d:%02d", hours, minutes, secs)
}

/**
 * 페이스 포맷팅 (초 -> M'SS")
 */
private fun formatPace(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d'%02d\"", minutes, secs)
}

/**
 * 캡션 레이블 포맷팅
 * "1일" -> "1일", "월" -> "월요일", "1월" -> "1월", "2024" -> "2024년"
 */
private fun formatCaptionLabel(label: String): String {
    return when {
        // 이미 "일"이 붙어있는 경우 (예: "14일")
        label.endsWith("일") -> label
        // "1월", "2월" 등 이미 "월" 포함
        label.endsWith("월") -> label
        // "월", "화" 등 한 글자 요일
        label.matches(Regex("[월화수목금토일]")) -> "${label}요일"
        // 4자리 숫자는 년도
        label.matches(Regex("\\d{4}")) -> "${label}년"
        // 주간 포맷 "W46" 등
        label.startsWith("W") -> label
        // 숫자만 있으면 일자 (월 그래프) - 하위호환성
        label.matches(Regex("\\d+")) -> "${label}일"
        // 기타
        else -> label
    }
}
