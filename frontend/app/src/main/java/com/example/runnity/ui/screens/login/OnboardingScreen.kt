package com.example.runnity.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PrimaryButton
import kotlinx.coroutines.launch

/**
 * 온보딩 화면
 * - 스와이프 가능한 페이지들
 * - 앱 기능 소개
 * - 마지막 페이지에서 "시작하기" 버튼
 *
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val pages = listOf(
        OnboardingPage(
            title = "함께 달리는 즐거움",
            description = "러니티와 함께\n실시간 챌린지에 참여하세요",
            imageRes = R.drawable.runnity_logo
        ),
        OnboardingPage(
            title = "나만의 기록 관리",
            description = "러닝 기록을 저장하고\n성장하는 나를 확인하세요",
            imageRes = R.drawable.runnity_logo
        ),
        OnboardingPage(
            title = "다양한 챌린지",
            description = "다른 러너들과 경쟁하며\n목표를 달성하세요",
            imageRes = R.drawable.runnity_logo
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Common.accent)
    ) {
        // 스와이프 가능한 페이지들
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // 페이지 인디케이터 (점들)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }

        // 마지막 페이지에서만 "시작하기" 버튼 표시
        if (pagerState.currentPage == pages.size - 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                PrimaryButton(
                    text = "시작하기",
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ColorPalette.Common.accent
                    )
                )
            }
        }
    }
}

/**
 * 온보딩 페이지 데이터 클래스
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

/**
 * 개별 온보딩 페이지 컨텐츠
 */
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 이미지
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        // 제목
        Text(
            text = page.title,
            style = Typography.Heading,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 설명
        Text(
            text = page.description,
            style = Typography.Body,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}
