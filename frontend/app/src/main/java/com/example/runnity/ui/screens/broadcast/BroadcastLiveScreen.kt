package com.example.runnity.ui.screens.broadcast

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.data.repository.BroadcastRepository
import com.example.runnity.data.util.DistanceUtils
import com.example.runnity.ui.components.LiveBadge
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.screens.broadcast.components.LiveRankingSection
import com.example.runnity.ui.screens.broadcast.components.MarathonTrackSection
import timber.log.Timber

/**
 * 브로드캐스트 라이브 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastLiveScreen(
    challengeId: String,
    navController: NavController,
    broadcastViewModel: BroadcastViewModel,
    liveViewModel: BroadcastLiveViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by liveViewModel.uiState.collectAsState()

    val leaveAction: () -> Unit = {
        liveViewModel.disconnectStomp()
        navController.popBackStack()
    }

    // STOMP 연결
    LaunchedEffect(challengeId) {
        Timber.d("[BroadcastUI] challengeId=${challengeId}")
        val initialData = broadcastViewModel.selectedBroadcast.value
        if (initialData != null) {
            // Live ViewModel을 목록 데이터로 초기화
            liveViewModel.initializeFrom(initialData)
        }
        // 중계방 입장 API → WebSocket 연결 자동 진행
        liveViewModel.joinAndConnect(challengeId.toLong())
    }

    // 화면 종료 시 정리
    DisposableEffect(Unit) {
        Timber.d("[BroadcastUI] 화면 종료")
        onDispose {
            liveViewModel.disconnectStomp()
        }
    }

    // 뒤로가기 버튼 처리
    BackHandler {
        Timber.d("[BroadcastUI] 뒤로가기")
        leaveAction()
    }



//
//    // 로딩 상태 표시
//    if (uiState.isLoading) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator()
//        }
//    }
//
//    // 에러 메시지 표시
//    uiState.errorMessage?.let { error ->
//        Snackbar(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(text = error)
//        }
//    }


    Scaffold { paddingValues ->
        // 화면의 실제 컨텐츠
        BroadcastLiveContent(
            modifier = modifier.padding(paddingValues),
            uiState = uiState, // liveViewModel의 uiState 사용
            onRunnerClick = { runnerId -> liveViewModel.selectRunner(runnerId) },
            onLeaveClick = {
                liveViewModel.disconnectStomp()
                navController.popBackStack()
            }
        )
    }
}

//    BroadcastLiveContent(
//        title = uiState.title,
//        viewerCount = uiState.viewerCount,
//        totalDistanceMeter = uiState.totalDistanceMeter,
//        runners = uiState.runners,
//        selectedRunnerId = uiState.selectedRunnerId,
//        onRunnerClick = { runnerId -> viewModel.selectRunner(runnerId) },
//        onLeaveClick = {
//            viewModel.disconnectStomp()
//            navController.navigateUp()
//        },
//        navController = navController,
//        modifier = modifier
//    )
//}
/**
 * 브로드캐스트 라이브 화면 컨텐츠
 */
@Composable
fun BroadcastLiveContent(
    uiState: BroadcastLiveViewModel.LiveUi,
    onRunnerClick: (Long?) -> Unit,
    onLeaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 실시간으로 순위 정렬
    val sortedRunners = remember(uiState.runners) {
        uiState.runners.sortedBy { it.rank }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            // 1. 헤더 (고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onLeaveClick) {
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
                    text = uiState.title.ifEmpty { uiState.title },
                    color = Color(0xFF222222),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = DistanceUtils.meterToKmString(uiState.totalDistanceMeter),
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 2. 시청자 수 (고정)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.viewerCount}명 시청 중",
                    color = Color(0xFF666666),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // 3. 마라톤 트랙 (고정)
            MarathonTrackSection(
                runners = sortedRunners.take(10),
                selectedRunnerId = uiState.selectedRunnerId,
                onRunnerClick = onRunnerClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            )

            Spacer(Modifier.height(8.dp))

            // 4. 실시간 순위표 (스크롤 가능)
            LiveRankingSection(
                runners = sortedRunners,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .padding(16.dp)
                    .padding(bottom = 70.dp)  // 하단 버튼 공간
            )
        }

        // 5. 하단 고정 버튼
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            PrimaryButton(
                text = "나가기",
                onClick = {
//                    navController.navigateUp()
                    onLeaveClick()
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

