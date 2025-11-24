package com.example.runnity.ui.screens.broadcast.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 마라톤 트랙 경로 좌표 계산 (타원형)
 * @param ratio 0.0 ~ 1.0 (완주 비율)
 * @param width 트랙 전체 너비
 * @param height 트랙 전체 높이
 * @param laneOffset 레인 오프셋 (-15f ~ 15f, 0이면 중앙 레인)
 */
fun calculateMarathonTrackPosition(
    ratio: Float,
    width: Float,
    height: Float,
    laneOffset: Float = 0f
): Pair<Float, Float> {
    val margin = 20f
    val centerX = width / 2
    val centerY = height / 2

    // 트랙 반지름 (외곽선 기준) + 레인 오프셋 적용
    val radiusX = (width / 2) - margin - 25f + laneOffset
    val radiusY = (height / 2) - margin - 25f + laneOffset

    // 타원 경로 (0% = 왼쪽 중앙, 시계방향)
    val angle = (ratio * 2 * PI).toFloat() - (PI / 2).toFloat()

    val x = centerX + radiusX * cos(angle.toDouble()).toFloat()
    val y = centerY + radiusY * sin(angle.toDouble()).toFloat()

    return Pair(x, y)
}
