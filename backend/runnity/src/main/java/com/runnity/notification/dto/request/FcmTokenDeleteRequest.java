package com.runnity.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Fcm 토큰 삭제 DTO")
public record FcmTokenDeleteRequest (
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Schema(description = "삭제할 FCM 토큰", example = "dY6sAfX-123abc_xyz...Uo")
        String token
){
}
