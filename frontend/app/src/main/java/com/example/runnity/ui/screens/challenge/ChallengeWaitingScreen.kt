package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.data.util.UserProfileManager
import com.example.runnity.socket.WebSocketManager
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SectionHeader
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChallengeWaitingScreen(
    challengeId: String,
    navController: NavController,
    viewModel: ChallengeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    socketViewModel: ChallengeSocketViewModel
) {
    val challengeIdLong = challengeId.toLongOrNull() ?: 0L

    // 상세 정보 및 대기방 참가자 정보 초기화
    LaunchedEffect(challengeIdLong) {
        if (challengeIdLong > 0) {
            viewModel.loadChallengeDetail(challengeIdLong)
            socketViewModel.observeSession(challengeIdLong)
        }
    }

    val challengeDetailState by viewModel.challengeDetail.collectAsState()
    val waitingParticipants by socketViewModel.participants.collectAsState()

    if (challengeDetailState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("대기방 정보를 불러오는 중...")
        }
        return
    }

    val detail = challengeDetailState!!

    // 챌린지 목표 거리(km)를 소켓 ViewModel에 설정 (완주자 랭킹 계산용)
    LaunchedEffect(challengeIdLong) {
        val goalKm = detail.distance.toDoubleOrNull()
        if (goalKm != null && goalKm > 0.0) {
            socketViewModel.setGoalKm(goalKm)
        }
    }

    // 챌린지 시작 시간 (카운트다운/표시용)
    val challengeStartTime = try {
        LocalDateTime.parse(detail.startAt, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        LocalDateTime.now().plusMinutes(10)
    }

    // 시작까지 5초 남았을 때 카운트다운 화면으로 자동 이동
    LaunchedEffect(challengeStartTime, challengeId) {
        try {
            val now = LocalDateTime.now()
            val secondsUntilStart = Duration.between(now, challengeStartTime).seconds
            when {
                secondsUntilStart > 5 -> {
                    kotlinx.coroutines.delay((secondsUntilStart - 5) * 1000)
                    navController.navigate("challenge_countdown/$challengeId")
                }
                secondsUntilStart in 1..5 -> {
                    // 이미 5초 이내라면 바로 카운트다운으로 이동
                    navController.navigate("challenge_countdown/$challengeId")
                }
                // 0 이하인 경우는 이미 시작 시간이 지난 상태이므로 아무 것도 하지 않음
            }
        } catch (_: Exception) {
            // 시간 계산 실패 시에는 자동 이동 생략
        }
    }

    // 현재 사용자 정보 (대기방 참여자 목록에서 "나"를 구분하기 위함)
    val profile = UserProfileManager.getProfile()
    val currentUserId = profile?.memberId?.toString().orEmpty()

    // 나를 상단에 고정한 참가자 리스트 구성 (대기방에서는 리타이어 참가자는 숨김)
    val participantsForUi = remember(waitingParticipants, currentUserId) {
        val visible = waitingParticipants.filterNot { it.isRetired }
        val me = visible.find { it.id == currentUserId }
        val others = visible.filter { it.id != currentUserId }
        if (me != null) listOf(me) + others else visible
    }

    var showQuitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 헤더: 기존 ChallengeDetailScreen과 동일 스타일
            ActionHeader(
                title = detail.title,
                onBack = {
                    WebSocketManager.close()
                    navController.navigateUp()
                },
                height = 56.dp
            )

            // 스크롤 가능한 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // 챌린지 시작 시간/카운트다운 카드
                ChallengeStartTimeSection(
                    startTime = challengeStartTime,
                    isReserved = detail.joined
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 챌린지 상세 정보 카드
                ChallengeDetailInfoSection(
                    description = detail.description,
                    distance = com.example.runnity.ui.screens.challenge.ChallengeMapper.formatDistance(detail.distance),
                    startTime = challengeStartTime,
                    maxParticipants = detail.maxParticipants,
                    currentParticipants = detail.currentParticipants,
                    isPublic = !detail.isPrivate,
                    hasBroadcast = detail.isBroadcast
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 참여자 섹션 헤더 (문구만 대기방용으로 변경)
                SectionHeader(
                    subtitle = "챌린지 참여자",
                    caption = "이 챌린지를 대기하는 참여자 목록입니다"
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (participantsForUi.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("아직 대기 중인 참여자가 없습니다.")
                    }
                } else {
                    ChallengeParticipantsSection(
                        participants = participantsForUi,
                        maxParticipants = detail.maxParticipants,
                        currentUserId = currentUserId
                    )
                }

                Spacer(modifier = Modifier.height(90.dp)) // 하단 버튼 공간 확보
            }
        }

        // 하단 고정 "챌린지 나가기" 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            PrimaryButton(
                text = "챌린지 나가기",
                onClick = { showQuitDialog = true }
            )
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("챌린지 나가기") },
            text = {
                Text(
                    text = "챌린지를 나가면 다시 참여할 수 없어요. 정말 나가시겠어요?",
                    style = Typography.Body,
                    color = ColorPalette.Light.primary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val quitJson = "{" +
                            "\"type\":\"QUIT\"," +
                            "\"timestamp\":" + System.currentTimeMillis() +
                            "}"
                        WebSocketManager.send(quitJson)
                        WebSocketManager.close()
                        showQuitDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("아니요")
                }
            }
        )
    }
}
