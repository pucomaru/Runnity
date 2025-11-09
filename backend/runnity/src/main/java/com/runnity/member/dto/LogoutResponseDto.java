package com.runnity.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "로그아웃 response DTO")
@Getter
@AllArgsConstructor
public class LogoutResponseDto {
    private String message;
}