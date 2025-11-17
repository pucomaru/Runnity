package com.example.runnity.ui.screens.broadcast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 상위 3위까지만 표시하는 순위판
 */
@Composable
fun BroadcastLiveRankingBoard(
    runners: List<BroadcastLiveViewModel.RunnerUi>,
    modifier: Modifier = Modifier
) {
    val top3 = remember(runners) {
        runners.sortedByDescending { it.distance }.take(3)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        top3.forEachIndexed { index, runner ->
            key(runner.runnerId) {
                TopRankItem(rank = index + 1, runner = runner)
            }
        }
    }
}

@Composable
fun TopRankItem(
    rank: Int,
    runner: BroadcastLiveViewModel.RunnerUi
) {
    val transition = updateTransition(targetState = rank, label = "rankTransition")

    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 500, easing = FastOutSlowInEasing) },
        label = "backgroundColor"
    ) { targetRank ->
        when (targetRank) {
            1 -> Color(0xFFFFD700)  // 금
            2 -> Color(0xFFC0C0C0)  // 은
            3 -> Color(0xFFCD7F32)  // 동
            else -> Color.Gray
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        // 순위 뱃지
        AnimatedContent(
            targetState = rank,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "rankBadge"
        ) { targetRank ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$targetRank",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 닉네임
        Text(
            text = runner.nickname,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        // 거리 (애니메이션 없이 즉시 표시)
        Text(
            text = "${String.format("%.2f", runner.distance / 1000f)}km",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
