package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 섹션 헤더 컴포넌트
 * - 서브타이틀과 캡션을 포함한 투명 박스 형태의 헤더
 * - 항목을 설명하는 용도로 사용
 *
 * @param subtitle 서브타이틀 텍스트 (예: "내가 참여한 챌린지")
 * @param caption 캡션 텍스트 (예: "내가 예약한 챌린지입니다.")
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * SectionHeader(
 *     subtitle = "내가 참여한 챌린지",
 *     caption = "내가 예약한 챌린지입니다."
 * )
 */
@Composable
fun SectionHeader(
    subtitle: String,              // 서브타이틀 (위쪽, 큰 텍스트)
    caption: String,               // 캡션 (아래쪽, 작은 텍스트)
    modifier: Modifier = Modifier  // 추가 Modifier (선택사항)
) {
    // Column: 세로로 UI 요소 배치
    Column(
        modifier = modifier
            .fillMaxWidth()                          // 가로 전체 크기 (화면 꽉 채움)
            .background(
                color = Color.Transparent,           // 투명 배경
                shape = RoundedCornerShape(8.dp)     // 모서리 둥글게 8dp
            )
            .padding(horizontal = 16.dp)             // 좌우 여백만 16dp (위아래 여백 없음)
    ) {
        // 서브타이틀 텍스트
        Text(
            text = subtitle,
            style = Typography.Subheading,           // 16px, Bold
            color = ColorPalette.Light.primary       // 검정색
        )

        // 서브타이틀과 캡션 사이 간격
        Spacer(modifier = Modifier.height(4.dp))

        // 캡션 텍스트
        Text(
            text = caption,
            style = Typography.Caption,              // 12px, Medium
            color = ColorPalette.Light.secondary     // 회색 (#848487)
        )
    }
}

/**
 * 캡션 없는 간단한 섹션 헤더
 * - 서브타이틀만 표시
 *
 * @param subtitle 서브타이틀 텍스트
 * @param modifier Modifier (선택사항)
 */
@Composable
fun SectionHeader(
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()                          // 가로 전체 크기 (화면 꽉 채움)
            .background(
                color = Color.Transparent,           // 투명 배경
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp)             // 좌우 여백만 16dp (위아래 여백 없음)
    ) {
        Text(
            text = subtitle,
            style = Typography.Subheading,
            color = ColorPalette.Light.primary
        )
    }
}

/**
 * 미리보기 (Preview)
 * - Android Studio에서 디자인 확인용
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF  // 흰색 배경
)
@Composable
private fun SectionHeaderPreview() {
    Column {
        // 서브타이틀 + 캡션
        SectionHeader(
            subtitle = "내가 참여한 챌린지",
            caption = "내가 예약한 챌린지입니다."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 서브타이틀만
        SectionHeader(
            subtitle = "전체 챌린지"
        )
    }
}
