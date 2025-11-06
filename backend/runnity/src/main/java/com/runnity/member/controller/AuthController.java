package com.runnity.member.controller;

import com.runnity.member.dto.LoginRequestDto;
import com.runnity.member.dto.LoginResponseDto;
import com.runnity.member.dto.TokenRequest;
import com.runnity.member.dto.TokenResponse;
import com.runnity.member.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    // 생성자를 통해 AuthService 주입받기
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login/google")
    public ResponseEntity<LoginResponseDto> googleLogin(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto response = authService.googleLogin(loginRequestDto.getIdToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/auth/login/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto response = authService.kakaoLogin(loginRequestDto.getIdToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Access Token 재발급
    @PostMapping("/auth/token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRequest request) {
        try {
            TokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}