package com.runnity.challenge.request;

import com.runnity.challenge.domain.ChallengeDistance;
import com.runnity.global.exception.GlobalException;
import com.runnity.global.status.ErrorStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Schema(description = "챌린지 목록 조회 요청")
public record ChallengeListRequest(

        @Schema(description = "검색 키워드 (챌린지 제목 일부)", example = "아침 러닝")
        String keyword,

        @Schema(description = "거리 필터 (예: FIVE, TEN, HALF - 여러 개 선택 가능)", implementation = ChallengeDistance.class, example = "[\"FIVE\", \"TEN\"]")
        List<ChallengeDistance> distances,

        @Schema(description = "시작 날짜 (이 날짜 이후 시작하는 챌린지만 조회, 최소 오늘)", example = "2025-11-14")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "종료 날짜 (이 날짜 이전에 시작하는 챌린지만 조회, 최대 일주일 후)", example = "2025-11-17")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "시작 시간 (이 시간 이후 시작하는 챌린지만 조회)", example = "09:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime startTime,

        @Schema(description = "종료 시간 (이 시간 이전에 시작하는 챌린지만 조회)", example = "11:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime endTime,

        @Schema(description = "공개 여부 (PUBLIC: 공개방만, ALL: 전체)", implementation = ChallengeVisibility.class, defaultValue = "PUBLIC")
        ChallengeVisibility visibility,

        @Schema(description = "정렬 기준 (POPULAR: 인기순, LATEST: 임박순)", implementation = ChallengeSortType.class, defaultValue = "POPULAR")
        ChallengeSortType sort
) {

    public ChallengeListRequest {
        keyword = (keyword != null && (keyword.isBlank() || "null".equalsIgnoreCase(keyword))) ? null : keyword;
        
        // 날짜 범위 유효성 검사
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7);
        
        if (startDate != null && startDate.isBefore(today)) {
            throw new GlobalException(ErrorStatus.CHALLENGE_DATE_BEFORE_TODAY);
        }
        
        if (endDate != null && endDate.isAfter(maxDate)) {
            throw new GlobalException(ErrorStatus.CHALLENGE_DATE_AFTER_MAX_LIMIT);
        }
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new GlobalException(ErrorStatus.CHALLENGE_INVALID_DATE_RANGE);
        }
        
        // 시간 범위 유효성 검사
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new GlobalException(ErrorStatus.CHALLENGE_INVALID_TIME_RANGE);
        }
        
        visibility = Objects.requireNonNullElse(visibility, ChallengeVisibility.PUBLIC);
        sort = Objects.requireNonNullElse(sort, ChallengeSortType.POPULAR);
    }
}
