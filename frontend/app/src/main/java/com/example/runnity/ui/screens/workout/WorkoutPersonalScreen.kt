package com.example.runnity.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

// 개인 러닝 화면
@Composable
fun WorkoutPersonalScreen(
    type: String?,
    km: String?,
    min: String?,
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=통계, 1=지도
    val isGoalTime = type == "time"
    val isGoalDistance = type == "distance"
    val isFreeRun = type == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {

        Column(Modifier.fillMaxWidth().statusBarsPadding()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsRun,
                        contentDescription = "통계",
                        tint = if (selectedTab == 0) ColorPalette.Light.primary else ColorPalette.Light.component
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = "지도",
                        tint = if (selectedTab == 1) ColorPalette.Light.primary else ColorPalette.Light.component
                    )
                }
            }
            // 선택된 탭 하단선만 표시
            Box(Modifier.fillMaxWidth().height(3.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .align(if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(ColorPalette.Common.accent)
                        .height(3.dp)
                )
            }
        }
        // 헤더와 본문 콘텐츠 사이 간격 (헤더 고정, 아래부터 1:1 분할)
        Spacer(modifier = Modifier.height(8.dp))

        // 본문: 정확히 상/하 절반 분할
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selectedTab == 0) {
                // 통계 탭 상단 영역 (목표 표시)
                val label = when {
                    isGoalTime -> "소요 시간"
                    isGoalDistance -> "킬로미터"
                    else -> "킬로미터" // 자유 달리기
                }
                val mainText = when {
                    isGoalTime -> "01:30" // placeholder
                    else -> "5.03" // 거리(자유/거리 목표)
                }

                Text(
                    text = label,
                    style = Typography.Subtitle,
                    color = ColorPalette.Light.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mainText,
                    style = Typography.LargeTitle.copy(fontSize = 72.sp),
                    color = ColorPalette.Light.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                if (!isFreeRun) {
                    // 목표 있을 때만 진행 바 표시 (UI placeholder, 고정 값)
                    LinearProgressIndicator(
                        progress = { 0.35f },
                        trackColor = Color(0xFFEAEAEA),
                        color = ColorPalette.Common.accent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            } else {
                // 지도 탭 상단 영역 (현재 위치 지도 자리) – UI placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "지도 영역", color = ColorPalette.Light.secondary)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ColorPalette.Light.containerBackground)
                .padding(horizontal = 16.dp, vertical = 16.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // 공통 메트릭 (2x2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem(label = "평균 페이스", value = "5'43\"/km")
                MetricItem(label = if (isGoalTime) "킬로미터" else "시간", value = if (isGoalTime) "5.03" else "1:20")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem(label = "평균 심박수", value = "163")
                MetricItem(label = "칼로리", value = "103")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 컨트롤 영역 (UI 상호작용: 일시정지 → 정지/재개 전환)
            var paused by remember { mutableStateOf(false) }
            if (!paused) {
                // Running: 일시정지 하나 (가로 중앙)
                CenterControlCircle(icon = "pause") { paused = true }
            } else {
                // Paused: 종료(길게3초) + 재개
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CenterControlCircle(icon = "stop", accent = ColorPalette.Common.stopAccent) { /* long-press TBD */ }
                    CenterControlCircle(icon = "play") { paused = false }
                }
            }
        }
    }
}

// 메트릭 아이템
@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = label, color = ColorPalette.Light.secondary, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = ColorPalette.Light.primary, style = Typography.Heading, textAlign = TextAlign.Center)
    }
}

// 단순한 원형 버튼
@Composable
private fun CenterControlCircle(icon: String, accent: Color = ColorPalette.Common.accent, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .background(accent, shape = androidx.compose.foundation.shape.CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 임시 텍스트 아이콘 표시 (UI 단계)
        val symbol = when (icon) {
            "pause" -> "Ⅱ"
            "play" -> "▶"
            "stop" -> "■"
            else -> "●"
        }
        Text(text = symbol, color = Color.White, style = Typography.Title)
    }
}
