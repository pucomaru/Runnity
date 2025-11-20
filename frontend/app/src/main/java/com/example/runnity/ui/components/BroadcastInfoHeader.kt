package com.example.runnity.ui.screens.broadcast.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 중계 정보 헤더
 *
 * 제목, 시청자 수, 참가자 수, 거리, 경과 시간 표시
 */
@Composable
fun BroadcastInfoHeader(
    title: String,
    viewerCount: Int,
    participantCount: Int,
    distance: String,
    totalDistanceMeter: Int,
    modifier: Modifier = Modifier
) {
    // 경과 시간 (초 단위)
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    Column(modifier = modifier) {
        // 제목
        Text(
            text = title.ifEmpty { "라이브 중계" },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 정보 카드들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LIVE 배지
            LiveBadge()

            // 시청자 수
            InfoChip(
                icon = Icons.Default.Person,
                label = "시청",
                value = "${viewerCount}명",
                modifier = Modifier.weight(1f)
            )

            // 참가자 수
            InfoChip(
                icon = Icons.Default.PlayArrow,
                label = "참가",
                value = "${participantCount}명",
                modifier = Modifier.weight(1f)
            )

            // 거리
            InfoChip(
                label = "거리",
                value = String.format("%.1f km", totalDistanceMeter / 1000f),
                modifier = Modifier.weight(1f)
            )

            // 경과 시간
            InfoChip(
                label = "경과",
                value = formatElapsedTime(elapsedSeconds),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * LIVE 배지
 */
@Composable
fun LiveBadge() {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            isVisible = !isVisible
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = if (isVisible) Color(0xFFE74C3C) else Color(0xFFC0392B),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "● LIVE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 정보 칩
 */
@Composable
fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF5F7FA), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

/**
 * 경과 시간 포맷팅 (MM:SS)
 */
fun formatElapsedTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
