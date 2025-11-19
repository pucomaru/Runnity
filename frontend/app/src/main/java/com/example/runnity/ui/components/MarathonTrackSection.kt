package com.example.runnity.ui.screens.broadcast.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.runnity.ui.screens.broadcast.BroadcastLiveViewModel
import com.example.runnity.ui.screens.broadcast.utils.calculateMarathonTrackPosition

/**
 * 마라톤 트랙 섹션 (상위 10명만 표시)
 */
@Composable
fun MarathonTrackSection(
    runners: List<BroadcastLiveViewModel.RunnerUi>,
    selectedRunnerId: Long?,
    onRunnerClick: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    trackWidth: Dp = 360.dp,
    trackHeight: Dp = 200.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(trackHeight)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onRunnerClick(null) }  // ← 외부 클릭 시 말풍선 닫기
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // 트랙 그리기
            DrawMarathonTrack(trackWidth = trackWidth, trackHeight = trackHeight)

            // 러너 마커들
            runners.forEach { runner ->
                MarathonRunnerMarker(
                    runner = runner,
                    isSelected = runner.runnerId == selectedRunnerId,
                    onClick = { onRunnerClick(runner.runnerId) },
                    trackWidth = trackWidth,
                    trackHeight = trackHeight
                )
            }
        }
    }
}

@Composable
fun BoxScope.MarathonRunnerMarker(
    runner: BroadcastLiveViewModel.RunnerUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    trackWidth: Dp,
    trackHeight: Dp
) {
    val animRatio by animateFloatAsState(
        targetValue = runner.ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "runnerRatio"
    )

    val density = LocalDensity.current
    val w = with(density) { trackWidth.toPx() }
    val h = with(density) { trackHeight.toPx() }

    val (xPx, yPx) = calculateMarathonTrackPosition(
        ratio = animRatio,
        width = w,
        height = h
    )

    val x = with(density) { (xPx - w / 2).toDp() }
    val y = with(density) { (yPx - h / 2).toDp() }

    // 닉네임 라벨 (항상 표시, 점 옆에 배치)
    // 점의 위치에 따라 닉네임 위치 조정 (점 위쪽에 표시)
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = x, y = y - 25.dp)  // 점 위에 닉네임 표시
            .zIndex(6f)
    ) {
        // 배경이 있는 닉네임 텍스트
        Box(
            modifier = Modifier
        ) {
            Text(
                text = runner.nickname,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1
            )
        }
    }

    // 말풍선 (선택되었을 때만 표시 - 상세 정보)
    if (isSelected) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = x, y = y - 60.dp)  // ← 점 위에 표시
                .zIndex(10f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = runner.nickname,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = runner.distanceKmFormatted,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "페이스: ${runner.paceFormatted}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }

    // ✅ 러너 점 (클릭 가능)
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = x, y = y)
            .size(20.dp)  // ← 클릭 영역 확대
            .clickable(onClick = onClick)
            .zIndex(5f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(runner.color, CircleShape)
        )
    }
}

/**
 * 마라톤 트랙 그리기 (타원형 경기장)
 */
@Composable
fun BoxScope.DrawMarathonTrack(trackWidth: Dp, trackHeight: Dp) {
    Canvas(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight)
            .align(Alignment.Center)
    ) {
        val w = size.width
        val h = size.height
        val margin = 15f
        val laneWidth = 50f

        // 외곽 트랙 (타원형)
        val trackPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = margin,
                    top = margin,
                    right = w - margin,
                    bottom = h - margin,
                    cornerRadius = CornerRadius(h / 2 - margin, h / 2 - margin)
                )
            )
        }

        // 트랙 외곽선
        drawPath(
            path = trackPath,
            color = Color(0xFFE74C3C),
            style = Stroke(width = 8f)
        )

        // 내부 트랙 라인
        for (i in 1..5) {
            val laneOffset = (laneWidth / 6) * i  // 레인 간격
            val innerPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = margin + laneOffset,
                        top = margin + laneOffset,
                        right = w - margin - laneOffset,
                        bottom = h - margin - laneOffset,
                        cornerRadius = CornerRadius(
                            (h / 2 - margin - laneOffset),
                            (h / 2 - margin - laneOffset)
                        )
                    )
                )
            }


            drawPath(
                path = innerPath,
                color = Color(0xFFBDC3C7),
                style = Stroke(
                    width = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f),  // 점선 패턴
                        0f
                    )
                )
            )
        }

        // 출발선 표시
        val startX = w / 2
        val startY = margin

        drawLine(
            color = Color(0xFF2ECC71),
            start = Offset(startX - laneWidth / 2, startY),
            end = Offset(startX + laneWidth / 2, startY),
            strokeWidth = 8f
        )

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#2ECC71")
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText("START", startX, startY - 10f, paint)
        }
    }
}
