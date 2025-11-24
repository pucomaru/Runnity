package com.example.runnity.ui.screens.broadcast

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.LiveBadge
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.screens.broadcast.components.MarathonTrackSection
import timber.log.Timber

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
        liveViewModel.disconnectStomp(challengeId.toLongOrNull())
        navController.popBackStack()
    }

    // WebSocket 연결
    LaunchedEffect(challengeId) {
        Timber.d("[BroadcastUI] challengeId=${challengeId}")
        val initialData = broadcastViewModel.selectedBroadcast.value
        if (initialData != null) {
            liveViewModel.initializeFrom(initialData)
        }
        liveViewModel.joinAndConnect(challengeId.toLong())
    }

    // 화면 종료 시 정리
    DisposableEffect(Unit) {
        Timber.d("[BroadcastUI] 화면 시작")
        onDispose {
            Timber.d("[BroadcastUI] 화면 종료")
            liveViewModel.disconnectStomp(challengeId.toLongOrNull())
        }
    }

    // 뒤로가기 처리
    BackHandler {
        Timber.d("[BroadcastUI] 뒤로가기")
        leaveAction()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 커스텀 헤더 (LIVE 표시는 제목 왼쪽, km는 오른쪽)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 뒤로가기 버튼
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = ColorPalette.Light.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { leaveAction() }
            )

            // 중앙: LIVE 배지 + 제목
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge(text = "LIVE")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = uiState.title.ifEmpty { "실시간 중계" },
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
            }

            // 오른쪽: km 거리
            Text(
                text = uiState.distance,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }

        HorizontalDivider(color = Color(0xFFDDDDDD))

        // 로딩 상태
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorPalette.Common.accent)
            }
            return
        }

        // 에러 상태
        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    style = Typography.Body,
                    color = ColorPalette.Light.secondary
                )
            }
            return
        }

        // 메인 컨텐츠
        BroadcastLiveContent(
            uiState = uiState,
            onRunnerClick = { liveViewModel.selectRunner(it) },
            onLeave = leaveAction
        )
    }
}

@Composable
private fun BroadcastLiveContent(
    uiState: BroadcastLiveViewModel.LiveUi,
    onRunnerClick: (Long?) -> Unit,
    onLeave: () -> Unit
) {
    val sortedRunners = remember(uiState.runners) {
        uiState.runners.sortedBy { it.rank }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 스크롤 가능한 컨텐츠 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. 라벨 행 (시청자 수)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "시청 인원",
                    tint = ColorPalette.Light.component,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${uiState.viewerCount}명 시청 중",
                    style = Typography.Caption,
                    color = ColorPalette.Light.secondary
                )
            }

            HorizontalDivider(color = Color(0xFFDDDDDD))

            // 2. 트랙 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "실시간 트랙",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                MarathonTrackSection(
                    runners = sortedRunners.take(10),
                    selectedRunnerId = uiState.selectedRunnerId,
                    onRunnerClick = onRunnerClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = Color(0xFFDDDDDD))

            // 3. 랭킹 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "실시간 순위",
                        style = Typography.Subheading,
                        color = ColorPalette.Light.primary
                    )
                    Text(
                        text = "${sortedRunners.size}명",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 랭킹 리스트
                if (sortedRunners.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "참가자가 없습니다",
                            style = Typography.Body,
                            color = ColorPalette.Light.secondary
                        )
                    }
                } else {
                    sortedRunners.forEachIndexed { index, runner ->
                        BroadcastRankingItem(runner = runner)
                        if (index < sortedRunners.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // 하단 고정 버튼
        Column {
            HorizontalDivider(color = Color(0xFFDDDDDD))
            PrimaryButton(
                text = "나가기",
                onClick = onLeave,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * 브로드캐스트 랭킹 아이템
 */
@Composable
private fun BroadcastRankingItem(
    runner: BroadcastLiveViewModel.RunnerUi
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPalette.Light.background, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽: 순위 + 프로필 이미지 + 닉네임
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.weight(1f)
        ) {
            // 순위
            Text(
                text = "${runner.rank}위",
                style = Typography.Subheading,
                color = when (runner.rank) {
                    1 -> Color(0xFFFFD700) // 금색
                    2 -> Color(0xFFC0C0C0) // 은색
                    3 -> Color(0xFFCD7F32) // 동색
                    else -> ColorPalette.Light.primary
                },
                modifier = Modifier.width(50.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 색상 마커
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(runner.color)
                    .border(
                        width = 1.dp,
                        color = ColorPalette.Light.component,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 닉네임
            Text(
                text = runner.nickname,
                style = Typography.Body,
                color = ColorPalette.Light.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 오른쪽: 거리 + 페이스
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 거리
            Text(
                text = runner.distanceKmFormatted,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )

            // 페이스
            Text(
                text = runner.paceFormatted,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=360dp,height=640dp,dpi=420"
)
@Composable
private fun BroadcastLiveScreenPreview() {
    // 샘플 데이터
    val sampleRunners = listOf(
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 1,
            nickname = "선호",
            profileImage = null,
            color = Color(0xFF3DDC84),
            distanceKm = 4.5,
            ratio = 0.9f,
            rank = 1,
            distanceKmFormatted = "4.50 km",
            paceFormatted = "5'20\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 2,
            nickname = "지훈",
            profileImage = null,
            color = Color(0xFFFF6F61),
            distanceKm = 4.4,
            ratio = 0.88f,
            rank = 2,
            distanceKmFormatted = "4.40 km",
            paceFormatted = "5'25\""
        ),
        BroadcastLiveViewModel.RunnerUi(
            runnerId = 3,
            nickname = "민수",
            profileImage = null,
            color = Color(0xFF42A5F5),
            distanceKm = 4.35,
            ratio = 0.87f,
            rank = 3,
            distanceKmFormatted = "4.35 km",
            paceFormatted = "5'30\""
        )
    )

    val sampleUiState = BroadcastLiveViewModel.LiveUi(
        title = "5km 마라톤",
        viewerCount = 127,
        participantCount = 3,
        distance = "5km",
        totaldistanceKm = 5000,
        hlsUrl = "",
        runners = sampleRunners,
        selectedRunnerId = null,
        highlightCommentary = null,
        isLoading = false,
        errorMessage = null
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF1E1E1E),
                    modifier = Modifier.size(24.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFF4444),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sampleUiState.title,
                        fontSize = 16.sp,
                        color = Color(0xFF1E1E1E)
                    )
                }

                Text(
                    text = sampleUiState.distance,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            HorizontalDivider(color = Color(0xFFDDDDDD))

            // 메인 컨텐츠
            BroadcastLiveContent(
                uiState = sampleUiState,
                onRunnerClick = {},
                onLeave = {}
            )
        }
    }
}
