package com.runnity.member.dto;

import com.runnity.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "프로필 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;
    @Schema(description = "닉네임", example = "러너1")
    private String nickname;
    @Schema(description = "성별", example = "MALE")
    private String gender;
    @Schema(description = "키(cm)", example = "175.4")
    private Float height;
    @Schema(description = "몸무게(kg)", example = "68.2")
    private Float weight;
    @Schema(description = "생년월일(yyyy-MM-dd)", example = "1998-09-17")
    private String birth;
    @Schema(description = "평균 페이스(초)", example = "305")
    private Integer averagePace;
    @Schema(description = "추가 정보 입력 필요 여부", example = "false")
    private Boolean needAdditionalInfo;

    public static ProfileResponseDto from(Member member) {
        boolean needInfo = member.getNickname() == null || member.getNickname().isBlank();

        return ProfileResponseDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImage())  // S3 Public URL 직접 저장됨
                .height(member.getHeight())
                .weight(member.getWeight())
                .gender(member.getGender())
                .birth(member.getBirth() != null ? member.getBirth().toString() : null)
                .averagePace(member.getAveragePace())
                .needAdditionalInfo(needInfo)
                .build();
    }
}