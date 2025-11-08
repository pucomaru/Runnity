package com.runnity.challenge.request;

import com.runnity.challenge.domain.ChallengeDistance;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "챌린지 목록 조회 요청")
public record ChallengeListRequest(

        @Schema(description = "검색 키워드 (챌린지 제목 일부)", example = "아침 러닝")
        String keyword,

        @Schema(description = "거리 필터 (예: FIVE, TEN, HALF)", implementation = ChallengeDistance.class, example = "FIVE")
        ChallengeDistance distance,

        @Schema(description = "시작 일시 (이 시간 이후 시작하는 챌린지만 조회)", example = "2025-11-06T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt,

        @Schema(description = "종료 일시 (이 시간 이전 종료되는 챌린지만 조회)", example = "2025-11-07T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAt,

        @Schema(description = "공개 여부 (PUBLIC: 공개방만, ALL: 전체)", implementation = ChallengeVisibility.class, defaultValue = "PUBLIC")
        ChallengeVisibility visibility,

        @Schema(description = "정렬 기준 (POPULAR: 인기순, LATEST: 최신순)", implementation = ChallengeSortType.class, defaultValue = "LATEST")
        ChallengeSortType sort,

        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
        Integer page,

        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        Integer size
) {

    public ChallengeListRequest {
        visibility = Objects.requireNonNullElse(visibility, ChallengeVisibility.PUBLIC);
        sort = Objects.requireNonNullElse(sort, ChallengeSortType.LATEST);
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size <= 0) ? 10 : size;
    }
}
