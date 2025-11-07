package com.runnity.challenge.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

@Builder
@Schema(description = "챌린지 목록 응답")
public record ChallengeListResponse(

        @Schema(description = "챌린지 목록 데이터")
        List<ChallengeListItemResponse> content,

        @Schema(description = "전체 챌린지 수", example = "120")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "12")
        int totalPages,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size
) {

    public static ChallengeListResponse from(Page<ChallengeListItemResponse> page) {
        if (page == null) return empty();

        return ChallengeListResponse.builder()
                .content(Collections.unmodifiableList(page.getContent()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    public static ChallengeListResponse empty() {
        return ChallengeListResponse.builder()
                .content(Collections.emptyList())
                .totalElements(0L)
                .totalPages(0)
                .page(0)
                .size(0)
                .build();
    }
}
