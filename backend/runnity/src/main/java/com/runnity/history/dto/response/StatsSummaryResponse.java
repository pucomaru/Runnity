package com.runnity.history.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record StatsSummaryResponse(
        Float totalDistance,           // 총 거리 (km)
        Integer totalTime,             // 총 운동 시간 (초)
        Float avgPace,                 // 평균 페이스 (초/km)
        Integer totalRunDays,          // 달린 날 수
        List<PeriodStatResponse> periodStats,  // 구간별 달린 거리
        List<RunRecordResponse> personals,   // 개인 러닝 최근 5개
        List<RunRecordResponse> challenges   // 챌린지 러닝 최근 5개
) {
}
