package com.example.runnity.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 중계방 카드 컴포넌트
 */
@Composable
fun BroadcastCard(
    title: String,                          // 제목
    viewerCount: Int,                       // 중계방 입장수
    participantCount: Int,                  // 챌린지 참여자 수
    distance: Int,                          // 거리
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 중계 아이콘
            Icon(
                imageVector = Icons.Default.LiveTv, // LiveTv 아이콘 사용
                contentDescription = "중계방",
                tint = ColorPalette.Common.accent,
                modifier = Modifier.size(40.dp)
            )

            // 중계 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 시청자 수
                    InfoChip(
                        icon = Icons.Default.Visibility,
                        text = "$viewerCount 명 시청 중"
                    )
                    // 참가자 수
                    InfoChip(
                        icon = Icons.Default.Group,
                        text = "$participantCount 명 참가 중"
                    )
                    // 거리
                    InfoChip(
                        icon = Icons.Default.TrackChanges,
                        text = "$distance km"
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorPalette.Light.secondary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = Typography.Caption,
            color = ColorPalette.Light.secondary
        )
    }
}

// 아이콘 의존성 추가 필요: implementation("androidx.compose.material:material-icons-extended")
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun BroadcastCardPreview() {
    BroadcastCard(
        title = "3km 달릴 사람 구한다",
        viewerCount = 120,
        participantCount = 15,
        distance = 10,
        onCardClick = {}
    )
}
