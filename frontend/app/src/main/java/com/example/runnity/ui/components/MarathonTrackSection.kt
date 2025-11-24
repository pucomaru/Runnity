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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.screens.broadcast.BroadcastLiveViewModel
import com.example.runnity.ui.screens.broadcast.utils.calculateMarathonTrackPosition
import kotlin.math.abs

/**
 * 비슷한 거리에 있는 러너들을 다른 레인에 배치
 * ratio 차이가 0.03 이내면 같은 그룹으로 간주
 */
private fun assignLanesToRunners(
    runners: List<BroadcastLiveViewModel.RunnerUi>
): List<Pair<BroadcastLiveViewModel.RunnerUi, Float>> {
    if (runners.isEmpty()) return emptyList()

    // ratio 기준으로 정렬
    val sortedRunners = runners.sortedBy { it.ratio }
    val result = mutableListOf<Pair<BroadcastLiveViewModel.RunnerUi, Float>>()

    // 레인 오프셋 범위: -12f ~ 12f (트랙 안쪽 ~ 바깥쪽)
    val laneOffsets = listOf(0f, -8f, 8f, -16f, 16f, -4f, 12f, -12f, 4f, -20f)

    var currentGroup = mutableListOf<BroadcastLiveViewModel.RunnerUi>()
    var lastRatio = -1f
    val groupThreshold = 0.03f // 3% 이내면 같은 그룹

    sortedRunners.forEach { runner ->
        if (lastRatio < 0f || abs(runner.ratio - lastRatio) <= groupThreshold) {
            // 같은 그룹에 추가
            currentGroup.add(runner)
        } else {
            // 이전 그룹 처리
            currentGroup.forEachIndexed { index, r ->
                val offset = laneOffsets[index % laneOffsets.size]
                result.add(r to offset)
            }
            // 새 그룹 시작
            currentGroup = mutableListOf(runner)
        }
        lastRatio = runner.ratio
    }

    // 마지막 그룹 처리
    currentGroup.forEachIndexed { index, r ->
        val offset = laneOffsets[index % laneOffsets.size]
        result.add(r to offset)
    }

    return result
}

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

            // 러너들을 ratio 기준으로 그룹화하여 레인 오프셋 계산
            val runnersWithLane = assignLanesToRunners(runners)

            // 러너 마커들
            runnersWithLane.forEach { (runner, laneOffset) ->
                MarathonRunnerMarker(
                    runner = runner,
                    isSelected = runner.runnerId == selectedRunnerId,
                    onClick = { onRunnerClick(runner.runnerId) },
                    trackWidth = trackWidth,
                    trackHeight = trackHeight,
                    laneOffset = laneOffset
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
    trackHeight: Dp,
    laneOffset: Float = 0f
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
        height = h,
        laneOffset = laneOffset
    )

    val x = with(density) { (xPx - w / 2).toDp() }
    val y = with(density) { (yPx - h / 2).toDp() }

    // 닉네임 라벨 (항상 표시, 점 위에 배치)
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = x, y = y - 25.dp)
            .zIndex(6f)
    ) {
        Text(
            text = runner.nickname,
            style = Typography.CaptionSmall,
            color = ColorPalette.Light.primary,
            maxLines = 1
        )
    }

    // 말풍선 (선택되었을 때만 표시 - 상세 정보)
    if (isSelected) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = x, y = y - 70.dp)
                .zIndex(10f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = runner.nickname,
                    style = Typography.Caption,
                    color = ColorPalette.Light.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = runner.distanceKmFormatted,
                    style = Typography.Body,
                    color = ColorPalette.Common.accent
                )
                Text(
                    text = "페이스: ${runner.paceFormatted}",
                    style = Typography.CaptionSmall,
                    color = ColorPalette.Light.secondary
                )
            }
        }
    }

    // 러너 마커 (클릭 가능)
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = x, y = y)
            .size(24.dp)
            .clickable(onClick = onClick)
            .zIndex(5f),
        contentAlignment = Alignment.Center
    ) {
        // 외곽 테두리 (흰색)
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color.White, CircleShape)
        )
        // 내부 색상
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(runner.color, CircleShape)
        )
    }
}

/**
 * 마라톤 트랙 그리기 (타원형 경기장)
 */
@Composable
fun BoxScope.DrawMarathonTrack(trackWidth: Dp, trackHeight: Dp) {
    val accentColor = ColorPalette.Common.accent
    val trackColor = Color(0xFFD84315)  // 진한 주황-빨강 트랙
    val trackShadow = Color(0xFFBF360C)  // 트랙 그림자
    val grassColor = Color(0xFF66BB6A)  // 선명한 녹색 잔디
    val grassDark = Color(0xFF4CAF50)   // 어두운 녹색 (입체감)

    Canvas(
        modifier = Modifier
            .width(trackWidth)
            .height(trackHeight)
            .align(Alignment.Center)
    ) {
        val w = size.width
        val h = size.height
        val margin = 10f
        val trackThickness = 50f

        // 1. 트랙 그림자 (입체감)
        val shadowPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = margin + 2f,
                    top = margin + 2f,
                    right = w - margin + 2f,
                    bottom = h - margin + 2f,
                    cornerRadius = CornerRadius(h / 2 - margin, h / 2 - margin)
                )
            )
        }
        drawPath(
            path = shadowPath,
            color = trackShadow.copy(alpha = 0.3f)
        )

        // 2. 외곽 트랙 (주황-빨강)
        val outerTrackPath = Path().apply {
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
        drawPath(
            path = outerTrackPath,
            color = trackColor
        )

        // 3. 내부 잔디밭
        val innerGrassPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = margin + trackThickness,
                    top = margin + trackThickness,
                    right = w - margin - trackThickness,
                    bottom = h - margin - trackThickness,
                    cornerRadius = CornerRadius(
                        (h / 2 - margin - trackThickness).coerceAtLeast(0f),
                        (h / 2 - margin - trackThickness).coerceAtLeast(0f)
                    )
                )
            )
        }
        // 잔디 베이스
        drawPath(
            path = innerGrassPath,
            color = grassColor
        )
        // 잔디 패턴 (수평 라인)
        for (i in 0..8) {
            val yPos = margin + trackThickness + (h - 2 * margin - 2 * trackThickness) * i / 8
            drawLine(
                color = grassDark.copy(alpha = 0.15f),
                start = Offset(margin + trackThickness, yPos),
                end = Offset(w - margin - trackThickness, yPos),
                strokeWidth = 3f
            )
        }

        // 4. 트랙 레인 라인 (흰색)
        for (i in 1..5) {
            val laneOffset = (trackThickness / 6) * i
            val lanePath = Path().apply {
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
                path = lanePath,
                color = Color.White.copy(alpha = 0.7f),
                style = Stroke(width = 1.5f)
            )
        }

        // 5. 트랙 외곽선
        drawPath(
            path = outerTrackPath,
            color = trackShadow,
            style = Stroke(width = 4f)
        )

        // 6. START/FINISH 라인 (상단 중앙)
        val startX = w / 2
        val startY = margin

        // 체커보드 패턴
        val checkerCount = 8
        val checkerWidth = trackThickness / checkerCount
        for (i in 0 until checkerCount) {
            val x1 = startX - trackThickness / 2 + checkerWidth * i
            val color = if (i % 2 == 0) Color.White else Color.Black
            drawLine(
                color = color,
                start = Offset(x1, startY),
                end = Offset(x1 + checkerWidth, startY),
                strokeWidth = 10f
            )
        }

        // 7. 거리 마커 (트랙 양쪽에 작은 표시)
        val markerPositions = listOf(0.25f, 0.5f, 0.75f)
        markerPositions.forEach { ratio ->
            // 왼쪽 마커
            val leftX = margin + 5f
            val leftY = margin + (h - 2 * margin) * ratio
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(leftX, leftY)
            )
            // 오른쪽 마커
            val rightX = w - margin - 5f
            val rightY = margin + (h - 2 * margin) * ratio
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(rightX, rightY)
            )
        }

        // 텍스트 라벨
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = Color.White.toArgb()
                textSize = 14f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
                setShadowLayer(2f, 0f, 1f, android.graphics.Color.BLACK)
            }
            drawText("START / FINISH", startX, startY - 5f, paint)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun MarathonTrackPreview() {
    // 샘플 러너 데이터 (비슷한 거리에 있는 러너들 포함)
    val sampleRunners = listOf(
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 1,
            nickname = "선호",
            profileImage = null,
            color = Color(0xFF3DDC84),
            distanceKm = 4.5,
            ratio = 0.9f,
            rank = 1,
            distanceKmFormatted = "4.50 km",
            paceFormatted = "5'20\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 2,
            nickname = "지훈",
            profileImage = null,
            color = Color(0xFFFF6F61),
            distanceKm = 4.4,
            ratio = 0.88f,
            rank = 2,
            distanceKmFormatted = "4.40 km",
            paceFormatted = "5'25\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 3,
            nickname = "민수",
            profileImage = null,
            color = Color(0xFF42A5F5),
            distanceKm = 4.35,
            ratio = 0.87f,
            rank = 3,
            distanceKmFormatted = "4.35 km",
            paceFormatted = "5'30\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 4,
            nickname = "영희",
            profileImage = null,
            color = Color(0xFFFFB300),
            distanceKm = 3.8,
            ratio = 0.76f,
            rank = 4,
            distanceKmFormatted = "3.80 km",
            paceFormatted = "5'45\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 5,
            nickname = "철수",
            profileImage = null,
            color = Color(0xFF7E57C2),
            distanceKm = 3.2,
            ratio = 0.64f,
            rank = 5,
            distanceKmFormatted = "3.20 km",
            paceFormatted = "6'00\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 6,
            nickname = "수진",
            profileImage = null,
            color = Color(0xFF26C6DA),
            distanceKm = 2.5,
            ratio = 0.5f,
            rank = 6,
            distanceKmFormatted = "2.50 km",
            paceFormatted = "6'15\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 7,
            nickname = "동현",
            profileImage = null,
            color = Color(0xFFEF5350),
            distanceKm = 1.8,
            ratio = 0.36f,
            rank = 7,
            distanceKmFormatted = "1.80 km",
            paceFormatted = "6'30\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 8,
            nickname = "하늘",
            profileImage = null,
            color = Color(0xFF66BB6A),
            distanceKm = 1.2,
            ratio = 0.24f,
            rank = 8,
            distanceKmFormatted = "1.20 km",
            paceFormatted = "6'45\""
        )
    )

    MarathonTrackSection(
        runners = sampleRunners,
        selectedRunnerId = null,
        onRunnerClick = {}
    )
}
