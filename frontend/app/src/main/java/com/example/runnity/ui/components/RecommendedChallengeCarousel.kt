package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 추천 챌린지 아이템 데이터
 *
 * @param id 챌린지 고유 ID
 * @param imageUrl 챌린지 이미지 URL (null이면 회색 placeholder)
 * @param title 챌린지 제목 (예: "러니티 추천 챌린지")
 * @param description 설명 (예: "대규모 러닝 실시간 경쟁")
 */
data class RecommendedChallengeItem(
    val id: String,
    val imageUrl: String? = null,  // 이미지 URL (null 가능)
    val title: String,
    val description: String
)

/**
 * 추천 챌린지 카드 컴포넌트
 * - 이미지 + 제목 + 설명
 * - 세로 방향 레이아웃
 *
 * @param imageUrl 이미지 URL (null이면 회색 placeholder)
 * @param title 제목
 * @param description 설명
 * @param onClick 카드 클릭 이벤트
 * @param modifier Modifier (선택사항)
 */
@Composable
private fun RecommendedChallengeCard(
    imageUrl: String?,                 // 이미지 URL
    title: String,                     // 제목
    description: String,               // 설명
    onClick: () -> Unit,               // 클릭 이벤트
    modifier: Modifier = Modifier      // 추가 Modifier
) {
    // 카드 전체
    Column(
        modifier = modifier
            .width(140.dp)             // 카드 너비 (변경)
            .clickable { onClick() }   // 카드 클릭 가능
    ) {
        // 1. 이미지 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),       // 이미지 높이 (변경)
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            if (imageUrl != null) {
                // 이미지 로드 (Coil 라이브러리 사용)
                // 참고: build.gradle에 Coil 라이브러리 추가 필요
                // implementation("io.coil-kt:coil-compose:2.5.0")
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,  // 이미지 크롭
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 이미지 없을 때 회색 placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorPalette.Light.component)  // 회색 (#9E9E9E)
                )
            }
        }

        // 2. 텍스트 영역
        Column(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // 제목
            Text(
                text = title,
                style = Typography.Body,        // 14px, Medium (변경)
                color = ColorPalette.Light.primary  // 검정색
            )

            // 설명
            Text(
                text = description,
                style = Typography.Caption,     // 12px, Medium
                color = ColorPalette.Light.secondary,  // 회색
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * 운영진 추천 챌린지 캐러셀 컴포넌트
 * - 가로 스크롤 가능한 추천 챌린지 리스트
 * - LazyRow 사용
 *
 * @param challenges 추천 챌린지 리스트
 * @param onChallengeClick 챌린지 클릭 이벤트 (id 전달)
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * Column {
 *     SectionHeader(
 *         subtitle = "운영진 추천 챌린지",
 *         caption = "이번 주 인기 챌린지"
 *     )
 *     RecommendedChallengeCarousel(
 *         challenges = listOf(
 *             RecommendedChallengeItem(
 *                 id = "1",
 *                 imageUrl = null,
 *                 title = "러니티 추천 챌린지",
 *                 description = "대규모 러닝 실시간 경쟁"
 *             )
 *         ),
 *         onChallengeClick = { id -> navController.navigate("detail/$id") }
 *     )
 * }
 */
@Composable
fun RecommendedChallengeCarousel(
    challenges: List<RecommendedChallengeItem>,  // 추천 챌린지 리스트
    onChallengeClick: (String) -> Unit,          // 클릭 이벤트 (챌린지 ID)
    modifier: Modifier = Modifier                // 추가 Modifier
) {
    // LazyRow: 가로 스크롤 가능한 리스트 (RecyclerView의 가로 버전)
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),  // 양쪽 여백
        horizontalArrangement = Arrangement.spacedBy(12.dp)  // 카드 간 간격
    ) {
        // items(): 리스트 데이터를 UI로 변환
        items(
            items = challenges,
            key = { it.id }  // 각 아이템의 고유 키 (성능 최적화)
        ) { challenge ->
            RecommendedChallengeCard(
                imageUrl = challenge.imageUrl,
                title = challenge.title,
                description = challenge.description,
                onClick = { onChallengeClick(challenge.id) }
            )
        }
    }
}

/**
 * 미리보기 (Preview)
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun RecommendedChallengeCarouselPreview() {
    val sampleChallenges = listOf(
        RecommendedChallengeItem(
            id = "1",
            imageUrl = null,  // 이미지 없음 (회색 placeholder)
            title = "러니티 추천 챌린지",
            description = "대규모 러닝 실시간 경쟁"
        ),
        RecommendedChallengeItem(
            id = "2",
            imageUrl = null,
            title = "주말 마라톤 챌린지",
            description = "함께 달리는 즐거움"
        ),
        RecommendedChallengeItem(
            id = "3",
            imageUrl = null,
            title = "초보 러너 환영",
            description = "천천히 함께 달려요"
        )
    )

    // 캐러셀만 표시
    RecommendedChallengeCarousel(
        challenges = sampleChallenges,
        onChallengeClick = {}
    )
}
