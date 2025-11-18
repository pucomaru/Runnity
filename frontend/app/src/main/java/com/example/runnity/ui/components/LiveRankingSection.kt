package com.example.runnity.ui.screens.broadcast.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.ui.screens.broadcast.BroadcastLiveViewModel

/**
 * 실시간 순위표 섹션
 */
@Composable
fun LiveRankingSection(
    runners: List<BroadcastLiveViewModel.RunnerUi>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "📊 실시간 순위 (${runners.size}명)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (runners.isEmpty()) {
            Text(
                text = "참가자가 없습니다",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
            ) {
                items(
                    items = runners,
                    key = { runner -> runner.runnerId }
                ) { runner ->
                    RankingItem(
                        rank = runners.indexOfFirst { it.runnerId == runner.runnerId } + 1,
                        runner = runner
                    )

                    if (runners.indexOf(runner) < runners.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFECF0F1)
                        )
                    }
                }
            }
        }
    }
}
