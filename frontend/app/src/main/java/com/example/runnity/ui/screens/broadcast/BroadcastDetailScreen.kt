package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
 * 중계 세부 정보 화면
 * - 하단 네비게이션 바 없음 (MainTabScreen에서 조건부로 숨김)
 * - 상단에 뒤로가기 버튼 있음
 * - 홈 화면과 중계 화면 모두에서 접근 가능
 *
 * @param challengeId 중계 고유 ID (route 파라미터로 전달됨)
 * @param navController 뒤로가기를 위한 NavController
 */
@Composable
fun ChallengeDetailScreen(
    challengeId: String,                    // URL 파라미터로 받은 중계 ID
    navController: NavController            // 뒤로가기용
) {
    // TODO: ViewModel에서 실제 중계 데이터 가져오기
    // 현재는 샘플 데이터 사용
    val challengeTitle = "3km 달릴 사람 구한다"
    val challengeDescription = "한강공원에서 같이 뛸 분들을 찾습니다. 편안한 페이스로 달려요!"
    val distance = "3km"
    val maxParticipants = 20
    val currentParticipants = 12
    val isPublic = true
    val hasBroadcast = false
    val isWebSocketConnected = false  // 웹소켓 연결 여부 (테스트용: false)

    // 중계 시작 시간 (테스트용: 현재 시간 + 4분 55초로 설정)
    val challengeStartTime = remember { LocalDateTime.now().plusMinutes(4).plusSeconds(55) }

    // 현재 사용자 ID (테스트용)
    val currentUserId = "user_1"

    // 참여자 리스트 샘플 데이터
    val allParticipants = listOf(
        Participant("user_1", "러너123", "https://example.com/avatar1.jpg", "5'30\""),
        Participant("user_2", "달리기왕", null, "6'00\""),
        Participant("user_3", "김철수", null, "5'45\""),
        Participant("user_4", "박영희", "https://example.com/avatar4.jpg", "6'15\""),
        Participant("user_5", "이민호", null, "5'20\""),
        Participant("user_6", "정수진", null, "6'30\""),
        Participant("user_7", "최동욱", null, "5'55\""),
        Participant("user_8", "한지민", null, "6'10\""),
        Participant("user_9", "오승호", null, "5'40\""),
        Participant("user_10", "윤서연", null, "6'20\""),
        Participant("user_11", "강태양", null, "5'50\""),
        Participant("user_12", "남궁민", null, "6'05\"")
    )

    // 나를 상단에 고정, 나머지는 그대로
    val participants = remember(allParticipants, currentUserId) {
        val me = allParticipants.find { it.id == currentUserId }
        val others = allParticipants.filter { it.id != currentUserId }
        if (me != null) listOf(me) + others else allParticipants
    }

    // 현재 시간 업데이트
    val now = remember { mutableStateOf(LocalDateTime.now()) }
    val secondsUntilStart = ChronoUnit.SECONDS.between(now.value, challengeStartTime)
    val isFiveMinutesBefore =  secondsUntilStart in 0..300

    LaunchedEffect(Unit) {
        while (isActive) {
            now.value = LocalDateTime.now()
            delay(1000)
        }
    }

    // 버튼 상태 결정
    val buttonText = "중계버튼"

    val buttonColor = ColorPalette.Common.accent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 상단 헤더 (뒤로가기 + 중계 이름)
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

                // 3. 중계 시작 시간 표시 영역
                ChallengeStartTimeSection(
                    startTime = challengeStartTime,
                    isReserved = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. 중계 상세 정보 섹션
                SectionHeader(
                    subtitle = "중계 상세 정보",
                    caption = "이 중계에 대한 자세한 정보입니다"
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

                // 5. 중계 참여자 섹션
                SectionHeader(
                    subtitle = "중계 참여자",
                    caption = "이 중계를 예약한 참여자 목록입니다"
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    // TODO: 버튼 클릭 처리
                    // - 예약하기: API 호출
                    // - 예약 취소하기: API 호출
                    // - 포기하기: 웹소켓 연결 해제 + API 호출
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            )
        }
    }
}

/**
 * 중계 시작 시간 표시 섹션 (카드)
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
                        text = "중계 시작까지",
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
                        text = "중계 시작 시간",
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
 * 중계 상세 정보 섹션 (카드)
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
 * 중계 참여자 섹션 (개별 카드)
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
    val averagePace: String  // 평균 페이스 (예: "5'30\"")
)
