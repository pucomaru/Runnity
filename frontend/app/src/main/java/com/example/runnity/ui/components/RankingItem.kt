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
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.screens.broadcast.BroadcastLiveViewModel

@Composable
fun RankingItem(
    rank: Int,
    runner: BroadcastLiveViewModel.RunnerUi,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (rank <= 3) ColorPalette.Common.accent.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                        else -> ColorPalette.Light.containerBackground
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = Typography.Body,
                color = if (rank <= 3) Color.White else ColorPalette.Light.primary
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
            style = Typography.Body,
            color = ColorPalette.Light.primary,
            modifier = Modifier.weight(1f)
        )

        // 거리 및 페이스
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = runner.distanceKmFormatted,
                style = Typography.Body,
                color = ColorPalette.Common.accent
            )
            Text(
                text = runner.paceFormatted,
                style = Typography.CaptionSmall,
                color = ColorPalette.Light.secondary
            )
        }
    }
}
