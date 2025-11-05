package com.example.runnity.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

// 액션 헤더 컴포넌트 (뒤로가기, 닫기 버튼 추가가능)
@Composable
fun ActionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    iconTint: Color = ColorPalette.Light.primary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back icon or spacer
        if (onBack != null) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowLeft,
                contentDescription = "뒤로가기",
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }

        // Center: Title
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.Heading,
                color = ColorPalette.Light.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Right: Close icon or spacer
        if (onClose != null) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "닫기",
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClose() }
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ActionHeaderPreview() {
    ActionHeader(
        title = "3km 달릴 사람 구한다",
        onBack = null,
        onClose = {},
    )
}
