package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 간단한 막대 그래프
 * @param data 그래프 데이터 포인트 리스트
 * @param modifier 수정자
 * @param showXAxisLabels X축 레이블 표시 여부 (기본값: 모두 표시)
 * @param xAxisLabelFilter X축 레이블 필터링 함수 (index, label) -> Boolean
 */
@Composable
fun SimpleBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    showXAxisLabels: Boolean = true,
    xAxisLabelFilter: ((Int, String) -> Boolean)? = null
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    // Y축 값 계산 (0부터 최댓값까지 4단계)
    val yAxisValues = remember(maxValue) {
        val step = maxValue / 4.0
        (0..4).map { (step * it).toInt().toDouble() }.reversed()
    }

    Column(modifier = modifier) {
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
                data.forEach { point ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // 값 표시
                        if (point.value > 0) {
                            Text(
                                text = point.value.toString(),
                                style = Typography.CaptionSmall,
                                color = ColorPalette.Light.secondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // 막대
                        val barHeight = if (maxValue > 0) {
                            (point.value / maxValue * 150).dp
                        } else {
                            0.dp
                        }

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(if (point.value > 0) barHeight else 1.dp)
                                .background(
                                    color = if (point.value > 0) ColorPalette.Common.accent else ColorPalette.Light.containerBackground,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
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
                        val shouldShowLabel = xAxisLabelFilter?.invoke(index, point.label) ?: true

                        Text(
                            text = if (shouldShowLabel) point.label else "",
                            style = Typography.CaptionSmall,
                            color = ColorPalette.Light.secondary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Spacer(modifier = Modifier.width(48.dp)) // Y축 레이블 공간만큼 패딩
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
