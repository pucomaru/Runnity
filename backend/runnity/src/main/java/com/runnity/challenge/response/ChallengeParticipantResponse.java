package com.runnity.challenge.response;

import com.runnity.challenge.domain.ChallengeParticipation;
import com.runnity.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 참여자 응답 DTO")
public record ChallengeParticipantResponse(
        @Schema(description = "회원 ID", example = "101")
        Long memberId,

        @Schema(description = "닉네임", example = "차은우")
        String nickname,

        @Schema(description = "프로필 이미지", example = "https://s3.bucket.com/profile/eunwoo.png")
        String profileImage,

        @Schema(description = "랭킹", example = "1")
        Integer rank,

        @Schema(description = "참여 상태", example = "JOINED")
        String status,

        @Schema(description = "평균 페이스 (초 단위)", example = "305")
        Integer averagePaceSec,

        @Schema(description = "해당 챌린지에서 기록한 페이스 (초 단위, 챌린지 완주 시에만 존재)", example = "310")
        Integer paceSec
) {
        public static ChallengeParticipantResponse fromHost(Member member) {
                return new ChallengeParticipantResponse(
                        member.getMemberId(),
                        member.getNickname(),
                        member.getProfileImage(),
                        null,
                        "WAITING",
                        member.getAveragePace(),
                        null
                );
        }

        public static ChallengeParticipantResponse from(ChallengeParticipation participation) {
                Member member = participation.getMember();
                Integer paceSec = participation.getRunRecord() != null
                        ? participation.getRunRecord().getPace()
                        : null;
                return new ChallengeParticipantResponse(
                        member.getMemberId(),
                        member.getNickname(),
                        member.getProfileImage(),
                        participation.getRanking(),
                        participation.getStatus().code(),
                        member.getAveragePace(),
                        paceSec
                );
        }
}
