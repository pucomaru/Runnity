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

    public static ProfileResponseDto from(Member m) {
        return ProfileResponseDto.builder()
                .memberId(m.getMemberId())
                .email(m.getEmail())
                .profileImageUrl(buildProxyUrl(m.getMemberId(), m.getProfileImage()))
                .nickname(m.getNickname())
                .gender(m.getGender())
                .height(m.getHeight())
                .weight(m.getWeight())
                .birth(m.getBirth())
                .build();
    }

    /**
     * S3 키 → 프록시 URL 변환
     */
    private static String buildProxyUrl(Long memberId, String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;  // 프로필 이미지 없음
        }
        // 프록시 API 경로 반환
        return "/api/v1/images/profile/" + memberId;
    }
}