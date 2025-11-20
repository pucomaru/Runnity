package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette

// 운동 + 지도 탭바 
@Composable
fun TabBar(
    items: List<ImageVector> = listOf(Icons.Outlined.DirectionsRun, Icons.Outlined.Map),
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 31.dp,
    selectedStroke: Dp = 3.dp,
    unselectedStroke: Dp = 1.dp,
    iconTint: Color = ColorPalette.Common.accent,
    strokeColor: Color = ColorPalette.Common.accent,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        items.forEachIndexed { index, icon ->
            val isSelected = index == selectedIndex
            val currentIconTint = if (isSelected) iconTint else ColorPalette.Light.component
            val currentStrokeColor = if (isSelected) strokeColor else ColorPalette.Light.component.copy(alpha = 0.5f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = currentIconTint
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSelected) selectedStroke else unselectedStroke)
                        .background(currentStrokeColor)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360,
    heightDp = 31
)
@Composable
private fun TabBarPreview() {
    TabBar(
        selectedIndex = 1,
        onSelected = {},
        modifier = Modifier.fillMaxWidth()
    )
}
