package com.runnity.challenge.dto;

import com.runnity.challenge.domain.Challenge;
import com.runnity.challenge.domain.ChallengeDistance;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(description = "챌린지 생성 요청 DTO")
public record ChallengeCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다")
        @Schema(description = "챌린지 제목", example = "아침 6시 러닝 챌린지")
        String title,

        @NotBlank(message = "설명은 필수입니다")
        @Schema(description = "챌린지 설명", example = "아침 6시에 함께 달리는 러닝 챌린지입니다.")
        String description,

        @NotNull(message = "최대 참가자 수는 필수입니다")
        @Min(value = 2, message = "최대 참가자 수는 최소 2명 이상이어야 합니다")
        @Max(value = 100, message = "최대 참가자 수는 100명을 초과할 수 없습니다")
        @Schema(description = "최대 참가자 수", example = "20")
        Integer maxParticipants,

        @NotNull(message = "시작 일시는 필수입니다")
        @Future(message = "시작 일시는 미래여야 합니다")
        @Schema(description = "시작 일시 (ISO-8601 형식)", example = "2025-11-03T18:00:00")
        LocalDateTime startAt,

        @NotNull(message = "거리는 필수입니다")
        @Schema(description = "거리 (ENUM 값: ONE, TWO, FIVE, HALF 등)", example = "FIVE")
        ChallengeDistance distance,

        @NotNull(message = "비밀방 여부는 필수입니다")
        @Schema(description = "비밀방 여부", example = "false")
        Boolean isPrivate,

        @Schema(description = "비밀번호 (비밀방일 때 필수)", example = "1234")
        String password,

        @NotNull(message = "중계방 여부는 필수입니다")
        @Schema(description = "중계방 여부", example = "false")
        Boolean isBroadcast
) {

    @AssertTrue(message = "시작일시는 현재 시점으로부터 1주일 이내여야 합니다")
    public boolean isStartWithinAWeek() {
        LocalDateTime now = LocalDateTime.now();
        return !startAt.isAfter(now.plusWeeks(1));
    }

    @AssertTrue(message = "비밀방일 경우 비밀번호를 입력해야 합니다")
    public boolean isPasswordValidForPrivateChallenge() {
        if (isPrivate) {
            return password != null && !password.isBlank();
        }
        return password == null || password.isBlank();
    }

    public Challenge toEntity() {
        return Challenge.builder()
                .title(this.title)
                .description(this.description)
                .maxParticipants(this.maxParticipants)
                .startAt(this.startAt)
                .distance(this.distance)
                .isPrivate(this.isPrivate)
                .password(password)
                .isBroadcast(this.isBroadcast)
                .build();
    }
}
