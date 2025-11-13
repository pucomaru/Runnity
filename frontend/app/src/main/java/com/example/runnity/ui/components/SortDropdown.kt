package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 정렬 드롭다운 컴포넌트
 * - 현재 선택된 정렬 기준을 표시하고 클릭 시 드롭다운 메뉴 표시
 * - 오른쪽 정렬로 배치됨
 * - 기본 옵션: "인기순", "최신순"
 *
 * @param selectedSort 현재 선택된 정렬 (예: "인기순")
 * @param onSortSelected 정렬 선택 이벤트
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * var selectedSort by remember { mutableStateOf("인기순") }
 * SortDropdown(
 *     selectedSort = selectedSort,
 *     onSortSelected = { selectedSort = it }
 * )
 */
@Composable
fun SortDropdown(
    selectedSort: String,                  // 현재 선택된 정렬
    onSortSelected: (String) -> Unit,      // 정렬 선택 콜백
    modifier: Modifier = Modifier          // 추가 Modifier
) {
    // 정렬 옵션 (고정) - 최신순이 먼저
    val sortOptions = listOf("최신순", "인기순")

    // 드롭다운 메뉴 표시 여부 상태
    var expanded by remember { mutableStateOf(false) }

    // Box: 드롭다운 메뉴를 위한 앵커 포인트
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd  // 오른쪽 상단 정렬
    ) {
        // 정렬 선택 버튼 (텍스트 + 화살표)
        Row(
            modifier = Modifier
                .clickable { expanded = true }  // 클릭 시 드롭다운 메뉴 표시
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 선택된 정렬 텍스트
            Text(
                text = selectedSort,
                style = Typography.Body,
                color = ColorPalette.Light.primary  // 검정색
            )

            // 드롭다운 화살표 아이콘
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "정렬 선택",
                tint = ColorPalette.Light.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // 드롭다운 메뉴
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },  // 메뉴 밖 클릭 시 닫기
            modifier = Modifier
                .background(Color.White)  // 흰색 배경
        ) {
            // 정렬 옵션들 (인기순, 최신순)
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = Typography.Body,
                            color = if (option == selectedSort) {
                                ColorPalette.Light.primary  // 선택된 항목: 검정색
                            } else {
                                ColorPalette.Light.secondary  // 일반 항목: 회색
                            }
                        )
                    },
                    onClick = {
                        onSortSelected(option)  // 정렬 선택
                        expanded = false        // 메뉴 닫기
                    }
                )
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
private fun SortDropdownPreview() {
    var selectedSort by remember { mutableStateOf("인기순") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "정렬 드롭다운",
            style = Typography.Title
        )

        SortDropdown(
            selectedSort = selectedSort,
            onSortSelected = { selectedSort = it }
        )

        Text(
            text = "현재 선택: $selectedSort",
            style = Typography.Body,
            color = ColorPalette.Light.component
        )
    }
}
