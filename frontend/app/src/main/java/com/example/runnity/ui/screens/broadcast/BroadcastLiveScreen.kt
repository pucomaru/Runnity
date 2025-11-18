package com.example.runnity.ui.screens.broadcast

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.ui.components.LiveBadge
import com.example.runnity.ui.components.PrimaryButton

@Composable
fun BroadcastLiveScreen(
    challengeId: String,
    navController: NavController,
    viewModel: BroadcastLiveViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(challengeId) {
        viewModel.connectStomp(challengeId)
        viewModel.preparePlayer(ui.hlsUrl)
    }

    LaunchedEffect(ui.hlsUrl) {
        viewModel.preparePlayer(ui.hlsUrl)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectStomp()
        }
    }

    // 스크롤 가능한 Column으로 전체 화면 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F7FA),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .verticalScroll(rememberScrollState())  // ← 스크롤 가능
            .padding(bottom = 80.dp)  // ← 하단 버튼 공간 확보
    ) {
        // 1) 상단 헤더 (뒤로가기 + 제목)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                text = ui.title,
                color = Color(0xFF222222),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2) LIVE 뱃지 + 시청자 수
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            LiveBadge(text = "LIVE")
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${ui.viewerCount}명 시청 중",
                color = Color(0xFF666666),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // 3) 상위 3위 순위표
        BroadcastLiveRankingBoard(
            runners = ui.runners,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 4) 단일 트랙 (모든 러너)
        BroadcastLiveRunnerTrackScreen(
            runners = ui.runners,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)

        )

        Spacer(Modifier.height(16.dp))

    }

    // 6) 하단 고정 버튼 (스크롤 영역 밖에 고정)
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        PrimaryButton(
            text = "나가기",
            onClick = { navController.navigateUp() },
            modifier = Modifier.padding(16.dp)
        )
    }
}
