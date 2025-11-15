package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SectionHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 챌린지 세부 정보 화면
 * - 하단 네비게이션 바 없음 (MainTabScreen에서 조건부로 숨김)
 * - 상단에 뒤로가기 버튼 있음
 * - 홈 화면과 챌린지 화면 모두에서 접근 가능
 *
 * @param challengeId 챌린지 고유 ID (route 파라미터로 전달됨)
 * @param navController 뒤로가기를 위한 NavController
 */
@Composable
fun ChallengeDetailScreen(
    challengeId: String,                    // URL 파라미터로 받은 챌린지 ID
    navController: NavController,           // 뒤로가기용
    viewModel: ChallengeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // 챌린지 ID를 Long으로 변환
    val challengeIdLong = challengeId.toLongOrNull() ?: 0L

    // ViewModel에서 챌린지 상세 정보 로드
    LaunchedEffect(challengeIdLong) {
        if (challengeIdLong > 0) {
            viewModel.loadChallengeDetail(challengeIdLong)
        }
    }

    // 챌린지 상세 정보 관찰
    val challengeDetail by viewModel.challengeDetail.collectAsState()

    // 다이얼로그 상태
    var showReserveDialog by remember { mutableStateOf(false) }
    var showCancelReserveDialog by remember { mutableStateOf(false) }

    // 비밀번호 입력 상태 (비공개 챌린지용)
    var passwordInput by remember { mutableStateOf("") }

    // 챌린지 정보가 없으면 로딩 표시
    if (challengeDetail == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("챌린지 정보를 불러오는 중...")
        }
        return
    }

    // 챌린지 정보 추출
    val detail = challengeDetail!!
    val challengeTitle = detail.title
    val challengeDescription = detail.description
    val distance = ChallengeMapper.formatDistance(detail.distance)
    val maxParticipants = detail.maxParticipants
    val currentParticipants = detail.currentParticipants
    val isPublic = !detail.isPrivate
    val hasBroadcast = detail.isBroadcast
    val isReserved = detail.joined
    val isWebSocketConnected = false  // TODO: 웹소켓 연결 상태 확인

    // 챌린지 시작 시간 파싱
    val challengeStartTime = try {
        LocalDateTime.parse(detail.startAt, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        LocalDateTime.now().plusMinutes(10)
    }

    // 현재 사용자 ID (TODO: UserProfileManager에서 가져오기)
    // 임시로 하드코딩, 추후 UserProfileManager.getProfile()?.memberId로 변경 필요
    val currentUserId = "1"

    // 참여자 리스트를 UI 모델로 변환
    val allParticipants = detail.participants.map { participant ->
        Participant(
            id = participant.memberId.toString(),
            nickname = participant.nickname,
            avatarUrl = participant.profileImage,
            averagePace = formatAveragePace(participant.averagePaceSec)
        )
    }

    // 나를 상단에 고정, 나머지는 그대로
    val participants = remember(allParticipants, currentUserId) {
        val me = allParticipants.find { it.id == currentUserId }
        val others = allParticipants.filter { it.id != currentUserId }
        if (me != null) listOf(me) + others else allParticipants
    }

    // 현재 시간 업데이트
    val now = remember { mutableStateOf(LocalDateTime.now()) }
    val secondsUntilStart = ChronoUnit.SECONDS.between(now.value, challengeStartTime)
    val isFiveMinutesBefore = isReserved && secondsUntilStart in 0..300

    LaunchedEffect(Unit) {
        while (isActive) {
            now.value = LocalDateTime.now()
            delay(1000)
        }
    }

    // 버튼 상태 결정
    val buttonText = when {
        !isReserved -> "챌린지 예약하기"
        isFiveMinutesBefore && isWebSocketConnected -> "챌린지 포기하기"
        isReserved -> "챌린지 예약 취소하기"
        else -> "챌린지 예약하기"
    }

    val buttonColor = when {
        !isReserved -> ColorPalette.Common.accent
        else -> ColorPalette.Common.stopAccent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 상단 헤더 (뒤로가기 + 챌린지 이름)
            ActionHeader(
                title = challengeTitle,
                onBack = { navController.navigateUp() },
                height = 56.dp
            )

            // 2. 스크롤 가능한 컨텐츠
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 3. 챌린지 시작 시간 표시 영역
                ChallengeStartTimeSection(
                    startTime = challengeStartTime,
                    isReserved = isReserved
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 챌린지 상세 정보 섹션
                SectionHeader(
                    subtitle = "챌린지 상세 정보",
                    caption = "이 챌린지에 대한 자세한 정보입니다"
                )

                Spacer(modifier = Modifier.height(12.dp))

                ChallengeDetailInfoSection(
                    description = challengeDescription,
                    distance = distance,
                    startTime = challengeStartTime,
                    maxParticipants = maxParticipants,
                    currentParticipants = currentParticipants,
                    isPublic = isPublic,
                    hasBroadcast = hasBroadcast
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. 챌린지 참여자 섹션
                SectionHeader(
                    subtitle = "챌린지 참여자",
                    caption = "이 챌린지를 예약한 참여자 목록입니다"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 참여자 목록 표시
                ChallengeParticipantsSection(
                    participants = participants,
                    maxParticipants = maxParticipants,
                    currentUserId = currentUserId
                )

                Spacer(modifier = Modifier.height(90.dp))  // 하단 버튼 공간 확보
            }
        }

        // 6. 하단 고정 버튼
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            PrimaryButton(
                text = buttonText,
                onClick = {
                    if (!isReserved) {
                        // 예약하기 다이얼로그 표시
                        showReserveDialog = true
                    } else if (isFiveMinutesBefore && isWebSocketConnected) {
                        // 웹소켓 연결 중인 경우 포기하기 (다이얼로그 없이 바로 처리)
                        // TODO: 웹소켓 포기 로직
                        viewModel.cancelChallenge(challengeIdLong)
                    } else {
                        // 예약 취소하기 다이얼로그 표시
                        showCancelReserveDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            )
        }
    }

    // 예약하기 확인 다이얼로그
    if (showReserveDialog) {
        AlertDialog(
            onDismissRequest = {
                showReserveDialog = false
                passwordInput = ""  // 다이얼로그 닫을 때 비밀번호 초기화
            },
            title = { Text("챌린지 예약") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("이 챌린지를 예약하시겠습니까?")

                    // 비공개 챌린지인 경우 비밀번호 입력 필드 표시
                    if (!isPublic) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                // 4자리 숫자만 입력 가능
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    passwordInput = it
                                }
                            },
                            label = { Text("비밀번호 (4자리)") },
                            placeholder = { Text("비밀번호를 입력하세요") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 비공개 챌린지인 경우 비밀번호 검증
                        if (!isPublic && passwordInput.length != 4) {
                            // 비밀번호가 4자리가 아니면 아무 것도 하지 않음
                            return@TextButton
                        }

                        // 비밀번호 전달 (공개 챌린지는 null)
                        val password = if (!isPublic) passwordInput else null
                        viewModel.joinChallenge(challengeIdLong, password)
                        showReserveDialog = false
                        passwordInput = ""  // 비밀번호 초기화
                    },
                    // 비공개 챌린지인데 비밀번호가 4자리가 아니면 버튼 비활성화
                    enabled = isPublic || passwordInput.length == 4
                ) {
                    Text("예약하기")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReserveDialog = false
                    passwordInput = ""  // 비밀번호 초기화
                }) {
                    Text("취소")
                }
            }
        )
    }

    // 예약 취소하기 확인 다이얼로그
    if (showCancelReserveDialog) {
        AlertDialog(
            onDismissRequest = { showCancelReserveDialog = false },
            title = { Text("챌린지 예약 취소") },
            text = { Text("이 챌린지 예약을 취소하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelChallenge(challengeIdLong)
                        showCancelReserveDialog = false
                    }
                ) {
                    Text("취소하기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelReserveDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }
}

/**
 * 챌린지 시작 시간 표시 섹션 (카드)
 * - 기본: accent 색으로 시작 날짜와 시간
 * - 5분 전: stopAccent 색상의 카운트다운
 */
@Composable
fun ChallengeStartTimeSection(
    startTime: LocalDateTime,
    isReserved: Boolean
) {
    val now = remember { mutableStateOf(LocalDateTime.now()) }
    val minutesUntilStart = ChronoUnit.MINUTES.between(now.value, startTime)
    val secondsUntilStart = ChronoUnit.SECONDS.between(now.value, startTime)

    // 시작 5분 전부터 카운트다운 표시
    val isFiveMinutesBefore = isReserved && secondsUntilStart in 0..300

    // 1초마다 업데이트
    LaunchedEffect(Unit) {
        while (isActive) {
            now.value = LocalDateTime.now()
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFiveMinutesBefore) ColorPalette.Common.stopAccent else ColorPalette.Common.accent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isFiveMinutesBefore) {
                // 5분 카운트다운 표시
                val minutes = secondsUntilStart / 60
                val seconds = secondsUntilStart % 60
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "챌린지 시작까지",
                        style = Typography.Body,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = Typography.Title.copy(fontSize = Typography.Title.fontSize * 1.5f),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // 일반 시작 시간 표시
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "챌린지 시작 시간",
                        style = Typography.Body,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = startTime.format(formatter),
                        style = Typography.Title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 챌린지 상세 정보 섹션 (카드)
 */
@Composable
fun ChallengeDetailInfoSection(
    description: String,
    distance: String,
    startTime: LocalDateTime,
    maxParticipants: Int,
    currentParticipants: Int,
    isPublic: Boolean,
    hasBroadcast: Boolean
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateTimeString = "${startTime.format(dateFormatter)} ${startTime.format(timeFormatter)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 설명
            Text(
                text = description,
                style = Typography.Body,
                color = ColorPalette.Light.primary
            )

            // 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorPalette.Light.component.copy(alpha = 0.2f))
            )

            // 정보 항목들 (2열 그리드)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 거리 & 시작 시간
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        label = "거리",
                        value = distance,
                        modifier = Modifier.weight(1f)
                    )
                    InfoItem(
                        label = "시작 시간",
                        value = dateTimeString,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 정원 & 공개 여부
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        label = "정원",
                        value = "$currentParticipants/$maxParticipants 명",
                        modifier = Modifier.weight(1f)
                    )
                    InfoItem(
                        label = "공개 여부",
                        value = if (isPublic) "공개" else "비공개",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 중계방
                InfoItem(
                    label = "중계방",
                    value = if (hasBroadcast) "사용" else "미사용"
                )
            }
        }
    }
}

/**
 * 정보 항목 (레이블 + 값)
 */
@Composable
fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = Typography.Caption,
            color = ColorPalette.Light.secondary
        )
        Text(
            text = value,
            style = Typography.Body,
            color = ColorPalette.Light.primary
        )
    }
}

/**
 * 챌린지 참여자 섹션 (개별 카드)
 */
@Composable
fun ChallengeParticipantsSection(
    participants: List<Participant>,
    maxParticipants: Int,
    currentUserId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 참여자 리스트 (각각 개별 카드)
        participants.forEach { participant ->
            ParticipantCard(
                participant = participant,
                isCurrentUser = participant.id == currentUserId
            )
        }
    }
}

/**
 * 참여자 카드 (ChallengeCard 스타일)
 */
@Composable
fun ParticipantCard(
    participant: Participant,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 프로필 이미지
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ColorPalette.Light.containerBackground)
                    .border(
                        width = if (isCurrentUser) 2.dp else 1.dp,
                        color = if (isCurrentUser) ColorPalette.Common.accent else ColorPalette.Light.component,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 실제 이미지 로딩 (Coil 라이브러리 사용)
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "프로필",
                    tint = if (isCurrentUser) ColorPalette.Common.accent else ColorPalette.Light.component,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 중간: 닉네임 + 나 배지
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = participant.nickname,
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
                if (isCurrentUser) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = ColorPalette.Common.accent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "나",
                            style = Typography.Caption,
                            color = Color.White
                        )
                    }
                }
            }

            // 오른쪽 끝: 평균 페이스
            Text(
                text = participant.averagePace,
                style = Typography.Body,
                color = ColorPalette.Light.secondary
            )
        }
    }
}

/**
 * 참여자 데이터 클래스
 */
data class Participant(
    val id: String,
    val nickname: String,
    val avatarUrl: String?,
    val averagePace: String,  // 평균 페이스 (예: "5'30\"")
    val distanceKm: Double = 0.0,       // 실시간 누적 거리 (km)
    val paceSecPerKm: Double? = null,   // 실시간 페이스 (sec/km)
    val rank: Int = 0,                  // 프론트에서 계산한 순위 (1,2,3,...)
    val isMe: Boolean = false           // 현재 사용자 여부
)

/**
 * 평균 페이스를 초 단위에서 분:초 형식으로 변환
 * 예: 305초 → "5'05\""
 */
private fun formatAveragePace(averagePaceSec: Int): String {
    val minutes = averagePaceSec / 60
    val seconds = averagePaceSec % 60
    return String.format("%d'%02d\"", minutes, seconds)
}
