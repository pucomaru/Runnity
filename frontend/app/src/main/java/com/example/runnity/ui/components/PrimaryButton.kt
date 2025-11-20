package com.example.runnity.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

// 메인 버튼 컴포넌트
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = ColorPalette.Common.accent,
        contentColor = Color.White,
        disabledContainerColor = ColorPalette.Common.accent.copy(alpha = 0.4f),
        disabledContentColor = Color.White.copy(alpha = 0.6f)
    ),
    border: BorderStroke? = null,
    leadingIcon: Painter? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            shape = RoundedCornerShape(12.dp),
            border = border,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Image(
                        painter = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = text, style = Typography.Subtitle)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PrimaryButtonPreview() {
    PrimaryButton(text = "Button", onClick = {})
}

