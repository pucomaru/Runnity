package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.components.LiveBadge
import com.example.runnity.ui.components.PrimaryButton

/**
 * 중계 세부 정보 화면
 * - 하단 네비게이션 바 없음 (MainTabScreen에서 조건부로 숨김)
 * - 상단에 뒤로가기 버튼 있음
 * - 홈 화면과 중계 화면 모두에서 접근 가능
 *
 * @param challengeId 중계 고유 ID (route 파라미터로 전달됨)
 * @param navController 뒤로가기를 위한 NavController
 */
@Composable
fun BroadcastLiveScreen(
    challengeId: String,                    // URL 파라미터로 받은 중계 ID
    navController: NavController,            // 뒤로가기용
    viewModel: BroadcastLiveViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState() // viewerCount, title, hlsUrl 등
    LaunchedEffect(challengeId) {
        viewModel.connectStomp(challengeId)     // STOMP 구독 시작
        viewModel.preparePlayer(ui.hlsUrl)               // ExoPlayer 준비
    }
    // hlsUrl이 나중에 오면 플레이어에 적용
    LaunchedEffect(ui.hlsUrl) {
        viewModel.preparePlayer(ui.hlsUrl)

    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectStomp()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // 1) 영상
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).also { it.player = viewModel.player }
            }
        )
        // 2) 상단 오버레이
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionHeader(
                title = ui.title,
                onBack = { navController.navigateUp() },
                height = 40.dp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadge(text = "LIVE")
                Spacer(Modifier.width(8.dp))
                Text(text = "${ui.viewerCount}명 시청 중", color = Color.White)
            }
        }
        // 3) 하단 고정 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            PrimaryButton(
                text = "나가기",
                onClick = { navController.navigateUp() }
            )
        }
    }
}
