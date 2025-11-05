package com.example.runnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 검색바 + 필터 아이콘 컴포넌트
 * - 검색 입력창과 필터 버튼이 한 줄에 배치
 * - 검색창: 왼쪽 (넓게), 필터 아이콘: 오른쪽
 *
 * @param searchQuery 검색어 (상태 값)
 * @param onSearchChange 검색어 변경 이벤트
 * @param onFilterClick 필터 아이콘 클릭 이벤트 (필터 페이지로 이동)
 * @param placeholder 검색창 placeholder (기본: "방 제목 검색")
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시:
 * var searchQuery by remember { mutableStateOf("") }
 * SearchBarWithFilter(
 *     searchQuery = searchQuery,
 *     onSearchChange = { searchQuery = it },
 *     onFilterClick = { navController.navigate("filter") }
 * )
 */
@Composable
fun SearchBarWithFilter(
    searchQuery: String,                   // 현재 검색어
    onSearchChange: (String) -> Unit,      // 검색어 변경 콜백
    onFilterClick: () -> Unit,             // 필터 버튼 클릭 콜백
    placeholder: String = "방 제목 검색",   // placeholder 텍스트
    modifier: Modifier = Modifier          // 추가 Modifier
) {
    // Row: 가로 배치 (검색창 + 필터 아이콘)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),  // 양쪽 여백
        horizontalArrangement = Arrangement.spacedBy(8.dp),  // 검색창과 아이콘 사이 간격
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 검색창 카드 (왼쪽, 남은 공간 차지)
        Card(
            modifier = Modifier
                .weight(1f)                // 남은 공간 모두 차지
                .height(48.dp),            // 높이 고정
            colors = CardDefaults.cardColors(
                containerColor = Color.White  // 흰색 배경
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp    // 그림자 높이 (리스트 카드와 동일)
            ),
            shape = RoundedCornerShape(8.dp)  // 모서리 둥글게 (필터 아이콘과 동일)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Row: 검색 아이콘 + 입력창
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 검색 아이콘
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "검색",
                        tint = ColorPalette.Light.component,  // 회색
                        modifier = Modifier.size(20.dp)
                    )

                    // 검색 입력창
                    Box(modifier = Modifier.weight(1f)) {
                        // placeholder 표시 (검색어가 비어있을 때)
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = Typography.Body,
                                color = ColorPalette.Light.component  // 회색
                            )
                        }

                        // BasicTextField: 실제 입력 필드
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            textStyle = Typography.Body.copy(
                                color = ColorPalette.Light.primary  // 검정색
                            ),
                            singleLine = true,  // 한 줄 입력
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2. 필터 아이콘 카드 (오른쪽)
        Card(
            modifier = Modifier.size(48.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White  // 흰색 배경
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp    // 그림자 높이 (리스트 카드와 동일)
            ),
            shape = RoundedCornerShape(8.dp)  // 모서리 둥글게
        ) {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "필터",
                    tint = ColorPalette.Light.primary,  // 검정색
                    modifier = Modifier.size(24.dp)
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
private fun SearchBarWithFilterPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // 빈 검색어 (placeholder 보임)
        SearchBarWithFilter(
            searchQuery = "",
            onSearchChange = {},
            onFilterClick = {}
        )

        // 검색어 입력됨
        SearchBarWithFilter(
            searchQuery = "3km 달리기",
            onSearchChange = {},
            onFilterClick = {}
        )
    }
}
