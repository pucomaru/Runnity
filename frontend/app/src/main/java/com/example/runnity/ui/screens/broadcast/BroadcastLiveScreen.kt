package com.example.runnity.ui.screens.broadcast

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.data.util.DistanceUtils
import com.example.runnity.ui.components.LiveBadge
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.screens.broadcast.components.LiveRankingSection
import com.example.runnity.ui.screens.broadcast.components.MarathonTrackSection

/**
 * 브로드캐스트 라이브 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastLiveScreen(
    challengeId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: BroadcastLiveViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // STOMP 연결
    LaunchedEffect(challengeId) {
        // 중계방 입장 API → WebSocket 연결 자동 진행
        viewModel.joinAndConnect(challengeId.toLong())
    }

    // 로딩 상태 표시
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // 에러 메시지 표시
    uiState.errorMessage?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = error)
        }
    }

    // 화면 종료 시 정리
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectStomp()
        }
    }

    // 뒤로가기 버튼 처리
    BackHandler {
        viewModel.disconnectStomp()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 시청자 수, 참가자 수 등 표시
                    Text(text = "시청자: ${uiState.viewerCount}", modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.errorMessage ?: "오류가 발생했습니다.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("돌아가기")
                        }
                    }
                }

                else -> {
                    // TODO: 여기에 실제 중계 화면 UI 구현 (지도, 러너 목록 등)
                    // 지금은 간단히 텍스트로 표시
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("거리: ${uiState.distance}")
                        Text("총 참가자: ${uiState.participantCount}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("참가자 순위:")
                        uiState.runners.forEach { runner ->
                            Text("${runner.rank}위: ${runner.nickname} (${runner.distanceMeter}m, ${runner.pace})")
                        }
                    }
                }
            }
        }
    }

    BroadcastLiveContent(
        title = uiState.title,
        viewerCount = uiState.viewerCount,
        totalDistanceMeter = uiState.totalDistanceMeter,
        runners = uiState.runners,
        selectedRunnerId = uiState.selectedRunnerId,
        onRunnerClick = { runnerId -> viewModel.selectRunner(runnerId) },
        navController = navController,
        modifier = modifier
    )
}

/**
 * 브로드캐스트 라이브 화면 컨텐츠
 */
@Composable
fun BroadcastLiveContent(
    title: String,
    viewerCount: Int,
    totalDistanceMeter: Int,
    runners: List<BroadcastLiveViewModel.RunnerUi>,
    selectedRunnerId: Long?,
    onRunnerClick: (Long?) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 실시간으로 순위 정렬
    val sortedRunners = remember(runners) {
        runners.sortedByDescending { it.distanceMeter }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            // ✅ 1. 헤더 (고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color(0xFF333333)
                    )
                }
                Spacer(Modifier.width(8.dp))

                LiveBadge(text = "LIVE")

                Spacer(Modifier.width(8.dp))

                Text(
                    text = title.ifEmpty { "라이브 중계" },
                    color = Color(0xFF222222),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = DistanceUtils.meterToKmString(totalDistanceMeter),
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ✅ 2. 시청자 수 (고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${viewerCount}명 시청 중",
                    color = Color(0xFF666666),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ✅ 3. 마라톤 트랙 (고정)
            MarathonTrackSection(
                runners = sortedRunners.take(10),
                selectedRunnerId = selectedRunnerId,
                onRunnerClick = onRunnerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // ✅ 4. 실시간 순위표 (스크롤 가능)
            LiveRankingSection(
                runners = sortedRunners,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // ← 남은 공간 모두 차지
                    .background(Color.White)
                    .padding(16.dp)
                    .padding(bottom = 70.dp)  // 하단 버튼 공간
            )
        }

        // ✅ 5. 하단 고정 버튼
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            PrimaryButton(
                text = "나가기",
                onClick = { navController.navigateUp() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
