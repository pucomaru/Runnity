package com.example.runnity.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 챌린지 카드의 버튼 상태
 *
 * - None: 버튼 없음 (기본 상태)
 * - Reserve: "예약하기" 버튼 표시 (시작 시간 5분 전 이전)
 * - CancelReserve: "예약 취소하기" 버튼 표시 (이미 예약한 챌린지)
 * - Join: "참가하기" 버튼 표시 (액센트 배경, 시작 5분 전부터 활성화)
 */
enum class ChallengeButtonState {
    None,           // 버튼 없음
    Reserve,        // 예약하기
    CancelReserve,  // 예약 취소하기
    Join            // 참가하기 (액센트 배경, 시작 5분 전)
}

/**
 * 챌린지 카드 컴포넌트
 * - 챌린지 정보를 카드 형태로 표시
 * - 거리 라벨, 제목, 날짜, 인원수, 버튼(조건부) 포함
 *
 * @param distance 거리 (예: "3km")
 * @param title 챌린지 제목 (예: "3km 달릴 사람 구한다")
 * @param startDateTime 시작 일시 (예: "2025.11.02 21:00 시작")
 * @param participants 참여 인원 (예: "15/100명")
 * @param buttonState 버튼 상태 (None, Join)
 * @param onCardClick 카드 클릭 이벤트 (세부 화면 이동용)
 * @param onButtonClick 버튼 클릭 이벤트 (참가하기)
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * ChallengeCard(
 *     distance = "3km",
 *     title = "3km 달릴 사람 구한다",
 *     startDateTime = "2025.11.02 21:00 시작",
 *     participants = "15/100명",
 *     buttonState = ChallengeButtonState.Join,  // 시작 5분 전
 *     onCardClick = { },
 *     onButtonClick = { }
 * )
 */
@Composable
fun ChallengeCard(
    distance: String,                          // 거리
    title: String,                             // 제목
    startDateTime: String,                     // 시작 일시
    participants: String,                      // 참여 인원
    isPrivate: Boolean = false,                // 비밀방 여부
    buttonState: ChallengeButtonState = ChallengeButtonState.None,  // 버튼 상태
    onCardClick: () -> Unit = {},              // 카드 클릭
    onButtonClick: () -> Unit = {},            // 버튼 클릭
    modifier: Modifier = Modifier              // 추가 Modifier
) {
    // Card: 그림자와 모서리가 둥근 카드 UI
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },      // 카드 전체 클릭 가능
        colors = CardDefaults.cardColors(
            containerColor = Color.White       // 흰색 배경
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp            // 그림자 높이
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)  // 모서리 둥글게
    ) {
        // Row: 가로 배치 (거리 라벨 + 정보 + 버튼)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),               // 카드 내부 여백
            horizontalArrangement = Arrangement.spacedBy(12.dp),  // 요소 간 간격
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 거리 라벨 (왼쪽)
            DistanceLabel(distance = distance)

            // 2. 챌린지 정보 (중간, 남은 공간 차지)
            Column(
                modifier = Modifier.weight(1f),  // 남은 공간 모두 차지
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 제목
                Text(
                    text = title,
                    style = Typography.Subheading,    // 16px, Bold
                    color = ColorPalette.Light.primary
                )

                // 시작 일시 (아이콘 + 텍스트)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "시작 시간",
                        tint = ColorPalette.Light.component,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = startDateTime,
                        style = Typography.Caption,       // 12px, Medium
                        color = ColorPalette.Light.secondary
                    )
                }

                // 참여 인원 (아이콘 + 텍스트 + 자물쇠)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "참여 인원",
                        tint = ColorPalette.Light.component,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = participants,
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                    if (isPrivate) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "비밀방",
                            tint = ColorPalette.Light.component,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // 3. 버튼 (오른쪽, 조건부 표시)
            when (buttonState) {
                ChallengeButtonState.Reserve -> {
                    // "예약하기" 버튼 (회색)
                    SmallPillButton(
                        text = "예약하기",
                        onClick = onButtonClick,
                        backgroundColor = ColorPalette.Light.background,
                        contentColor = ColorPalette.Light.primary,
                        enableSelectionEffect = false
                    )
                }
                ChallengeButtonState.CancelReserve -> {
                    // "예약 취소하기" 버튼 (회색)
                    SmallPillButton(
                        text = "예약 취소",
                        onClick = onButtonClick,
                        backgroundColor = ColorPalette.Light.background,
                        contentColor = ColorPalette.Light.primary,
                        enableSelectionEffect = false
                    )
                }
                ChallengeButtonState.Join -> {
                    // "참가하기" 버튼 (액센트 색상)
                    SmallPillButton(
                        text = "참가하기",
                        onClick = onButtonClick,
                        backgroundColor = ColorPalette.Common.accent,  // 액센트 색상
                        contentColor = Color.White,
                        enableSelectionEffect = false
                    )
                }
                ChallengeButtonState.None -> {
                    // 버튼 없음 (빈 공간도 없음)
                }
            }
        }
    }
}

/**
 * 미리보기 (Preview)
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFF4F4F4
)
@Composable
private fun ChallengeCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // 참가하기 버튼 (시작 5분 전)
        ChallengeCard(
            distance = "5km",
            title = "주말 아침 런닝",
            startDateTime = "2025.11.03 08:00 시작",
            participants = "8/20명",
            buttonState = ChallengeButtonState.Join
        )

        // 버튼 없음 (기본 상태)
        ChallengeCard(
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        )

        // 버튼 없음 (예약된 챌린지)
        ChallengeCard(
            distance = "10km",
            title = "한강 야간 러닝",
            startDateTime = "2025.11.05 20:00 시작",
            participants = "3/15명",
            buttonState = ChallengeButtonState.None
        )
    }
}
