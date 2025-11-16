package com.example.runnity.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 날씨 카드 컴포넌트
 * - 현재 위치의 날씨 정보 표시
 * - 배경 이미지 + 그라데이션 오버레이
 *
 * @param country 국가 (예: "Korea")
 * @param city 도시 (예: "Seoul")
 * @param weather 날씨 상태 (예: "Cloudy")
 * @param temperature 온도 (예: "10°")
 * @param time 시간 (예: "9:41 AM")
 * @param backgroundImageUrl 배경 이미지 URL (null이면 회색 placeholder)
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * WeatherCard(
 *     country = "Korea",
 *     city = "Seoul",
 *     weather = "Cloudy",
 *     temperature = "10°",
 *     time = "9:41 AM"
 * )
 */
@Composable
fun WeatherCard(
    country: String,               // 국가
    city: String,                  // 도시
    weather: String,               // 날씨 상태
    temperature: String,           // 온도
    time: String,                  // 시간
    @DrawableRes backgroundImageRes: Int? = null,  // 배경 이미지 리소스
    backgroundImageUrl: String? = null,  // 배경 이미지 URL (deprecated)
    onClick: (() -> Unit)? = null, // 클릭 이벤트
    onRefresh: (() -> Unit)? = null, // 새로고침 클릭 이벤트
    modifier: Modifier = Modifier  // 추가 Modifier
) {
    // Card: 날씨 정보 카드
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)       // 카드 높이
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        // Box: 배경 이미지 + 텍스트 오버레이
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 배경 이미지
            when {
                backgroundImageRes != null -> {
                    // 리소스 이미지 사용
                    Image(
                        painter = painterResource(id = backgroundImageRes),
                        contentDescription = "날씨 배경",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                backgroundImageUrl != null -> {
                    // URL 이미지 사용 (이전 버전 호환)
                    AsyncImage(
                        model = backgroundImageUrl,
                        contentDescription = "날씨 배경",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // 이미지 없을 때 회색 배경
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ColorPalette.Light.component)
                    )
                }
            }

            // 2. 그라데이션 오버레이 (텍스트 가독성 향상)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 3. 날씨 정보 텍스트
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 위치 + 날씨 상태
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 국가
                    Text(
                        text = country,
                        style = Typography.Caption,
                        color = Color.White
                    )

                    // 도시
                    Text(
                        text = city,
                        style = Typography.Title,  // 24px, Bold
                        color = Color.White
                    )

                    // 날씨 상태
                    Text(
                        text = weather,
                        style = Typography.Subheading,  // 16px, Bold
                        color = Color.White
                    )
                }

                // 오른쪽: 시간 + 온도
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 시간 + 새로고침 아이콘
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 시간
                        Text(
                            text = time,
                            style = Typography.Caption,
                            color = Color.White
                        )

                        // 새로고침 아이콘 (시간 텍스트 크기와 동일)
                        if (onRefresh != null) {
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier.size(16.dp)  // Caption 크기와 비슷하게
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "날씨 새로고침",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // 온도
                    Text(
                        text = temperature,
                        style = Typography.LargeTitle.copy(fontSize = 48.sp),  // 큰 온도 표시
                        color = Color.White
                    )
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
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun WeatherCardPreview() {
    WeatherCard(
        country = "Korea",
        city = "Seoul",
        weather = "Cloudy",
        temperature = "10°",
        time = "9:41 AM",
        backgroundImageUrl = null,
        modifier = Modifier.padding(16.dp)
    )
}
