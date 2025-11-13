package com.runnity.challenge.request;

import com.runnity.challenge.domain.ChallengeDistance;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Schema(description = "챌린지 목록 조회 요청")
public record ChallengeListRequest(

        @Schema(description = "검색 키워드 (챌린지 제목 일부)", example = "아침 러닝")
        String keyword,

        @Schema(description = "거리 필터 (예: FIVE, TEN, HALF - 여러 개 선택 가능)", implementation = ChallengeDistance.class, example = "[\"FIVE\", \"TEN\"]")
        List<ChallengeDistance> distances,

        @Schema(description = "시작 일시 (이 시간 이후 시작하는 챌린지만 조회)", example = "2025-11-06T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt,

        @Schema(description = "종료 일시 (이 시간 이전에 시작하는 챌린지만 조회)", example = "2025-11-07T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAt,

        @Schema(description = "공개 여부 (PUBLIC: 공개방만, ALL: 전체)", implementation = ChallengeVisibility.class, defaultValue = "PUBLIC")
        ChallengeVisibility visibility,

        @Schema(description = "정렬 기준 (POPULAR: 인기순, LATEST: 임박순)", implementation = ChallengeSortType.class, defaultValue = "POPULAR")
        ChallengeSortType sort
) {

    public ChallengeListRequest {
        keyword = (keyword != null && (keyword.isBlank() || "null".equalsIgnoreCase(keyword))) ? null : keyword;
        
        visibility = Objects.requireNonNullElse(visibility, ChallengeVisibility.PUBLIC);
        sort = Objects.requireNonNullElse(sort, ChallengeSortType.POPULAR);
    }
}
