package com.example.runnity.ui.screens.broadcast.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실시간 순위",
                style = Typography.Subheading,
                color = ColorPalette.Light.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${runners.size}명",
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }

        if (runners.isEmpty()) {
            Text(
                text = "참가자가 없습니다",
                style = Typography.Body,
                color = ColorPalette.Light.secondary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = runners,
                    key = { runner -> runner.runnerId }
                ) { runner ->
                    RankingItem(
                        rank = runner.rank,
                        runner = runner
                    )

                    if (runners.indexOf(runner) < runners.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = ColorPalette.Light.containerBackground
                        )
                    }
                }
            }
        }
    }
}
