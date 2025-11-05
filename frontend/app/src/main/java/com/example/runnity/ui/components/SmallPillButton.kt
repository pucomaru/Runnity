package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

// 작은 페일 버튼 컴포넌트
@Composable
fun SmallPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 33,
    minWidthDp: Int = 52,
    selected: Boolean = false,
    enableSelectionEffect: Boolean = true,
    backgroundColor: Color = Color.White,
    selectedBackgroundColor: Color = ColorPalette.Common.accent,
    contentColor: Color = ColorPalette.Light.secondary,
    selectedContentColor: Color = Color.White,
    cornerRadiusDp: Int = 8,
) {
    val shape = RoundedCornerShape(cornerRadiusDp.dp)
    val currentBg = if (enableSelectionEffect && selected) selectedBackgroundColor else backgroundColor
    val currentFg = if (enableSelectionEffect && selected) selectedContentColor else contentColor

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .defaultMinSize(minWidth = minWidthDp.dp)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(currentBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = Typography.Body, color = currentFg)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SmallPillButtonPreview() {
    SmallPillButton(text = "3km", onClick = {})
}
