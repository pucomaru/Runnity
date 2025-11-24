package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.screens.broadcast.components.MarathonTrackSection

/**
 * 백엔드 서버 없이 UI를 확인하기 위한 독립 실행형 Preview 화면
 * 실제 앱에서는 사용되지 않습니다.
 */
@Composable
fun BroadcastLiveScreenPreviewStandalone() {
    // Mock 데이터
    val mockRunners = remember {
        listOf(
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
            ),
            BroadcastLiveViewModel.RunnerUi(
                runnerId = 4,
                nickname = "영희",
                profileImage = null,
                color = Color(0xFFFFB300),
                distanceKm = 3.8,
                ratio = 0.76f,
                rank = 4,
                distanceKmFormatted = "3.80 km",
                paceFormatted = "5'45\""
            ),
            BroadcastLiveViewModel.RunnerUi(
                runnerId = 5,
                nickname = "철수",
                profileImage = null,
                color = Color(0xFF7E57C2),
                distanceKm = 3.2,
                ratio = 0.64f,
                rank = 5,
                distanceKmFormatted = "3.20 km",
                paceFormatted = "6'00\""
            ),
            BroadcastLiveViewModel.RunnerUi(
                runnerId = 6,
                nickname = "수진",
                profileImage = null,
                color = Color(0xFF26C6DA),
                distanceKm = 2.5,
                ratio = 0.5f,
                rank = 6,
                distanceKmFormatted = "2.50 km",
                paceFormatted = "6'15\""
            )
        )
    }

    val mockUiState = BroadcastLiveViewModel.LiveUi(
        title = "5km 마라톤 대회",
        viewerCount = 7,
        participantCount = 6,
        distance = "5km",
        totaldistanceKm = 5000,
        hlsUrl = "",
        runners = mockRunners,
        selectedRunnerId = null,
        highlightCommentary = null,
        isLoading = false,
        errorMessage = null
    )

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
                modifier = Modifier.size(24.dp)
            )

            // 중앙: LIVE 배지 + 제목
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LIVE 배지
                Surface(
                    color = Color(0xFFFF4444),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        style = Typography.CaptionSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mockUiState.title,
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )
            }

            // 오른쪽: km 거리
            Text(
                text = mockUiState.distance,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }

        HorizontalDivider(color = Color(0xFFDDDDDD))

        // 메인 컨텐츠
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
                        text = "${mockUiState.viewerCount}명 시청 중",
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
                        runners = mockRunners.take(10),
                        selectedRunnerId = null,
                        onRunnerClick = {},
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
                            text = "${mockRunners.size}명",
                            style = Typography.Caption,
                            color = ColorPalette.Light.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 랭킹 리스트
                    mockRunners.forEachIndexed { index, runner ->
                        MockRankingItem(runner = runner)
                        if (index < mockRunners.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 하단 고정 버튼
            Column {
                HorizontalDivider(color = Color(0xFFDDDDDD))
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPalette.Common.accent
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "나가기",
                        style = Typography.Body,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MockRankingItem(runner: BroadcastLiveViewModel.RunnerUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPalette.Light.background, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽: 순위 + 색상 마커 + 닉네임
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
            Text(
                text = runner.distanceKmFormatted,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )

            Text(
                text = runner.paceFormatted,
                style = Typography.Caption,
                color = ColorPalette.Light.secondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BroadcastLiveScreenPreviewDemo() {
    BroadcastLiveScreenPreviewStandalone()
}
