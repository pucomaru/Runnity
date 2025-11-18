package com.example.runnity.ui.screens.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.key
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.PrimaryButton

/**
 * 프로필 섹션
 */
@Composable
fun ProfileSection(
    userProfile: UserProfile,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (userProfile.profileImageUrl != null && userProfile.profileImageUrl.isNotBlank()) {
                    // 프로필 이미지 URL이 있을 경우
                    rememberAsyncImagePainter(userProfile.profileImageUrl)
                } else {
                    // 프로필 이미지가 없을 경우 기본 이미지
                    painterResource(id = R.drawable.profile)
                },
                contentDescription = "프로필",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 닉네임
        Text(
            text = userProfile.nickname,
            style = Typography.Heading,
            color = ColorPalette.Light.primary,
            modifier = Modifier.weight(1f)
        )

        // 편집 버튼
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "프로필 수정",
            modifier = Modifier
                .size(24.dp)
                .clickable { onEditClick() },
            tint = ColorPalette.Light.component
        )
    }
}

/**
 * Scope Bar (주/월/연/전체)
 */
@Composable
fun ScopeBar(
    selectedPeriodType: PeriodType,
    onPeriodTypeSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp)
            .background(
                color = ColorPalette.Light.containerBackground,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val items = listOf(
            PeriodType.WEEK to "주",
            PeriodType.MONTH to "월",
            PeriodType.YEAR to "연",
            PeriodType.ALL to "전체"
        )

        items.forEach { (type, label) ->
            key(type) {
                val isSelected = selectedPeriodType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (isSelected) ColorPalette.Common.accent else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onPeriodTypeSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = Typography.Body,
                        color = if (isSelected) Color.White else ColorPalette.Light.secondary
                    )
                }
            }
        }
    }
}

/**
 * 기간 선택 버튼
 */
@Composable
fun PeriodSelector(
    selectedText: String,
    onClick: () -> Unit,
    showDropdownIcon: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp) // 고정 높이 설정
            .then(
                if (showDropdownIcon) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedText,
            style = Typography.Subheading,
            color = ColorPalette.Light.primary
        )
        if (showDropdownIcon) {
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "기간 선택",
                tint = ColorPalette.Light.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 통계 섹션
 */
@Composable
fun StatsSection(stats: RunningStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 총 거리 (소수점 첫째 자리까지)
        Text(
            text = String.format("%.1f Km", stats.totalDistance),
            style = Typography.LargeTitle,
            color = ColorPalette.Light.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 세부 통계
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem(label = "${stats.runningDays}일", sublabel = "달린 날")
            StatItem(label = stats.averagePace, sublabel = "평균 페이스")
            StatItem(label = stats.totalTime, sublabel = "시간")
        }
    }
}

@Composable
private fun StatItem(label: String, sublabel: String) {
    Column {
        Text(
            text = label,
            style = Typography.Subheading,
            color = ColorPalette.Light.primary
        )
        Text(
            text = sublabel,
            style = Typography.Caption,
            color = ColorPalette.Light.secondary
        )
    }
}

/**
 * 러닝 기록 탭 섹션
 */
@Composable
fun RunningRecordTabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    personalRecords: List<RunningRecord>,
    challengeRecords: List<RunningRecord>,
    onViewAllClick: () -> Unit,
    onRecordClick: (RunningRecord) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 탭바 - 흰색 배경
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White)
        ) {
            IconTab(
                icon = Icons.Filled.DirectionsRun,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            IconTab(
                icon = Icons.Filled.Group,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // 리스트 영역 - 회색 배경
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.Light.containerBackground)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 기록 리스트
            val records = if (selectedTab == 0) personalRecords else challengeRecords

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "기록이 없습니다",
                        style = Typography.Body,
                        color = ColorPalette.Light.secondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    records.take(5).forEach { record ->
                        key(record.id) {
                            RunningRecordItem(
                                record = record,
                                onClick = { onRecordClick(record) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 모든 기록 보기 버튼
            PrimaryButton(
                text = "모든 운동 기록",
                onClick = onViewAllClick
            )
        }
    }
}

/**
 * 아이콘 탭
 */
@Composable
private fun IconTab(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ColorPalette.Common.accent else ColorPalette.Light.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSelected) 3.dp else 1.dp)
                .background(if (isSelected) ColorPalette.Common.accent else ColorPalette.Light.component)
        )
    }
}

/**
 * 러닝 기록 아이템 (ChallengeCard 스타일)
 */
@Composable
private fun RunningRecordItem(
    record: RunningRecord,
    onClick: () -> Unit = {}
) {
    // 개인/챌린지에 따라 아이콘 선택
    val icon = if (record.type == "challenge") {
        Icons.Filled.Group
    } else {
        Icons.Filled.DirectionsRun
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = ColorPalette.Light.containerBackground,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (record.type == "challenge") "챌린지" else "러닝",
                    tint = ColorPalette.Common.accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 정보 (중간)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 거리
                Text(
                    text = "${record.distance} km",
                    style = Typography.Subheading,
                    color = ColorPalette.Light.primary
                )

                // 날짜
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "날짜",
                        tint = ColorPalette.Light.component,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = record.date,
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                }

                // 페이스와 시간
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "평균 페이스 ${record.pace}",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                    Text(
                        text = "시간 ${record.time}",
                        style = Typography.Caption,
                        color = ColorPalette.Light.secondary
                    )
                }
            }
        }
    }
}
