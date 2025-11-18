package com.runnity.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Fcm 토큰 저장 DTO")
public record FcmTokenRegisterRequest(

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Schema(description = "발급된 FCM 토큰", example = "dY6sAfX-123abc_xyz...Uo")
        String token
) {
}
