package com.example.runnity.ui.screens.broadcast.utils

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 마라톤 트랙 경로 좌표 계산 (타원형)
 */
fun calculateMarathonTrackPosition(
    ratio: Float,
    width: Float,
    height: Float
): Pair<Float, Float> {
    val margin = 20f
    val centerX = width / 2
    val centerY = height / 2

    // 트랙 반지름 (외곽선 기준)
    val radiusX = (width / 2) - margin - 25f
    val radiusY = (height / 2) - margin - 25f

    // 타원 경로 (0% = 왼쪽 중앙, 시계방향)
    val angle = (ratio * 2 * PI).toFloat() - (PI / 2).toFloat()

    val x = centerX + radiusX * cos(angle.toDouble()).toFloat()
    val y = centerY + radiusY * sin(angle.toDouble()).toFloat()

    return Pair(x, y)
}
