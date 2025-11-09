package com.runnity.member.controller;

import com.runnity.global.status.ErrorStatus;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.*;
import com.runnity.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Auth", description = "소셜 로그인, 토큰 재발급 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login/google")
    @Operation(summary = "구글 로그인", description = "구글 ID 토큰으로 로그인합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "ID 토큰 검증 실패")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LoginResponseDto>> googleLogin(
            @RequestBody LoginRequestDto request
    ) {
        try {
            LoginResponseDto resp = authService.googleLogin(request.getIdToken());
            return com.runnity.global.response.ApiResponse.success(
                    SuccessStatus.LOGIN_SUCCESS, resp
            );
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/auth/login/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 ID 토큰으로 로그인합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "ID 토큰 검증 실패")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LoginResponseDto>> kakaoLogin(
            @RequestBody LoginRequestDto request
    ) {
        try {
            LoginResponseDto resp = authService.kakaoLogin(request.getIdToken());
            return com.runnity.global.response.ApiResponse.success(
                    SuccessStatus.LOGIN_SUCCESS, resp
            );
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(
            value = "/auth/addInfo",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    @Operation(
            summary = "추가 정보 입력 (신규 회원)",
            description = "로그인 후 닉네임, 키, 몸무게, 성별, 생년월일을 입력합니다. 프로필 사진은 선택사항입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 정보 입력 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<AddInfoResponseDto>> addAdditionalInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("data") AddInfoRequestDto request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            if (request == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
            }

            if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
            }

            // 서비스 호출
            authService.addAdditionalInfo(userPrincipal.getMemberId(), request, profileImage);

            AddInfoResponseDto response = new AddInfoResponseDto("추가 정보가 성공적으로 저장되었습니다");
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK, response);
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음"),
            @ApiResponse(responseCode = "404", description = "회원 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/me/profile")
    public ResponseEntity<com.runnity.global.response.ApiResponse<ProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            ProfileResponseDto profile = authService.getProfile(userPrincipal.getMemberId());
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK, profile);

        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.NOT_FOUND);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/auth/token")
    @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 새로운 Access/Refresh Token을 발급합니다")
    public ResponseEntity<com.runnity.global.response.ApiResponse<TokenResponseDto>> refreshToken(
            @RequestBody TokenRequestDto request
    ) {
        try {
            TokenResponseDto resp = authService.refreshAccessToken(request.getRefreshToken());
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK, resp);
        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/auth/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 Access Token을 블랙리스트에 등록하여 로그아웃합니다. " +
                    "클라이언트에서도 저장된 토큰을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<LogoutResponseDto>> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request
    ) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            // Authorization 헤더에서 토큰 추출
            String accessToken = extractTokenFromRequest(request);
            if (accessToken == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            // 로그아웃 처리 (블랙리스트 등록)
            authService.logout(userPrincipal.getMemberId(), accessToken);
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}