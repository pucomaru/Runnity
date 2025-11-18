package com.example.runnity.ui.screens.broadcast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 세로 방향 단일 트랙 + 모든 러너 마커 표시
 * - 아래(0%) → 위(100%) 이동
 * - 도착 시 ratio = 1.0f에서 멈춤
 */
@Composable
fun BroadcastLiveRunnerTrackScreen(
    runners: List<BroadcastLiveViewModel.RunnerUi>,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 400.dp,  // ← 세로 길이
    trackWidth: Dp = 8.dp,     // ← 선 두께
    markerRadius: Dp = 12.dp,
    showTop: Int = 10  // ← 상위 10명만 표시
) {
    if (runners.isEmpty()) return

    var selectedRunnerId by remember { mutableStateOf<Long?>(null) }

    // 상위 N명만 필터링 (성능 최적화)
    val topRunners = remember(runners) {
        runners.sortedByDescending { it.distanceMeter }.take(showTop)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "경기 진행 상황 (상위 ${showTop}명)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .width(80.dp)  // ← 트랙 영역 너비
                    .height(trackHeight),
                contentAlignment = Alignment.BottomCenter  // ← 하단 기준 정렬
            ) {
                // 세로 트랙 그리기
                VerticalTrackLine(
                    trackHeight = trackHeight,
                    trackWidth = trackWidth
                )

                // 러너 마커들
                topRunners.forEach { runner ->
                    VerticalRunnerMarker(
                        runner = runner,
                        trackHeight = trackHeight,
                        markerRadius = markerRadius,
                        isSelected = selectedRunnerId == runner.runnerId,
                        onClick = {
                            selectedRunnerId = if (selectedRunnerId == runner.runnerId) {
                                null
                            } else {
                                runner.runnerId
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 세로 트랙 라인
 */
@Composable
fun BoxScope.VerticalTrackLine(
    trackHeight: Dp,
    trackWidth: Dp
) {
    val tWidthPx = with(LocalDensity.current) { trackWidth.toPx() }

    Canvas(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight)
            .align(Alignment.BottomCenter)
    ) {
        val x = size.width / 2f
        // 하단(0%) → 상단(100%)으로 세로 선
        drawLine(
            color = Color(0xFFDDDDDD),
            start = Offset(x, size.height),  // ← 하단 시작
            end = Offset(x, 0f),              // ← 상단 끝
            strokeWidth = tWidthPx,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 세로 방향 러너 마커
 */
@Composable
fun BoxScope.VerticalRunnerMarker(
    runner: BroadcastLiveViewModel.RunnerUi,
    trackHeight: Dp,
    markerRadius: Dp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val clampedRatio = runner.ratio.coerceIn(0f, 1f)

    val animRatio by animateFloatAsState(
        targetValue = clampedRatio,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.75f),
        label = "runnerRatio"
    )

    val offsetY = trackHeight * animRatio

    // 점 (마커)
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = -offsetY)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Canvas(
            modifier = Modifier.size(markerRadius * 2)
        ) {
            drawCircle(
                color = runner.color,
                radius = size.minDimension / 2
            )
        }
    }

    // 말풍선 (점과 별개로 배치)
    AnimatedVisibility(
        visible = isSelected,
        enter = fadeIn() + slideInHorizontally(),
        exit = fadeOut() + slideOutHorizontally(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(
                x = markerRadius * 2 + 16.dp,  // ← 오른쪽 고정 위치
                y = -offsetY  // ← 점과 동일한 Y 위치
            )
    ) {
        RunnerInfoBubble(runner = runner)
    }
}

/**
 * 러너 정보 말풍선
 */
@Composable
fun RunnerInfoBubble(runner: BroadcastLiveViewModel.RunnerUi) {
    Row(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 삼각형 꼬리
        Canvas(
            modifier = Modifier.size(8.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width, size.height / 2)
                lineTo(0f, 0f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = Color.White)
        }

        Spacer(Modifier.width(4.dp))

        // 정보 표시
        Column {
            Text(
                text = runner.nickname,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${runner.rank}위 · ${String.format("%.2f", runner.distanceMeter / 1000f)}km",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = "페이스: ${runner.pace}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
