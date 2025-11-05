package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 거리 라벨 컴포넌트
 * - 챌린지 거리를 표시하는 작은 사각형 라벨
 * - 회색 배경에 검정 텍스트
 *
 * @param distance 거리 텍스트 (예: "3km", "5km")
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * DistanceLabel(distance = "3km")
 */
@Composable
fun DistanceLabel(
    distance: String,              // 거리 텍스트
    modifier: Modifier = Modifier  // 추가 Modifier
) {
    // Box: 중앙 정렬이 필요한 단일 UI 요소
    Box(
        modifier = modifier
            .size(48.dp)                             // 정사각형 48x48dp
            .background(
                color = ColorPalette.Light.containerBackground,  // 회색 배경 (#F4F4F4)
                shape = RoundedCornerShape(8.dp)     // 모서리 둥글게 8dp
            )
            .padding(4.dp),                          // 내부 여백
        contentAlignment = Alignment.Center          // 텍스트 중앙 정렬
    ) {
        Text(
            text = distance,
            style = Typography.Body,                 // 14px, Medium
            color = ColorPalette.Light.primary       // 검정색
        )
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
private fun DistanceLabelPreview() {
    DistanceLabel(distance = "3km")
}
