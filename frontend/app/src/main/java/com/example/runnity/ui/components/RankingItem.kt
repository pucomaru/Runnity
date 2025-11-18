package com.example.runnity.ui.screens.broadcast.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.ui.screens.broadcast.BroadcastLiveViewModel

@Composable
fun RankingItem(
    rank: Int,
    runner: BroadcastLiveViewModel.RunnerUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier  // ← 적용
            .fillMaxWidth()
            .background(
                color = if (rank <= 3) Color(0xFFFFF9E6) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 순위
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> Color(0xFFECF0F1)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.White else Color.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 색상 마커
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(runner.color, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 닉네임
        Text(
            text = runner.nickname,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        // 거리
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.2f km", runner.distanceMeter / 1000f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "페이스: ${runner.pace}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
