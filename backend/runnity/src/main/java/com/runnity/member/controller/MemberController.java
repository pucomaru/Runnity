package com.runnity.member.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runnity.global.status.ErrorStatus;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.*;
import com.runnity.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Auth", description = "소셜 로그인, 토큰 재발급 API")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AmazonS3 amazonS3;
    private final ObjectMapper objectMapper;

    @Value("${AWS_BUCKET:AWS_BUCKET}")
    private String bucket;

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
            LoginResponseDto resp = memberService.googleLogin(request.getIdToken());
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
            LoginResponseDto resp = memberService.kakaoLogin(request.getIdToken());
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
            @RequestPart("data") String dataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            if (dataJson == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
            }

            AddInfoRequestDto request = objectMapper.readValue(dataJson, AddInfoRequestDto.class);

            // 서비스 호출
            memberService.addAdditionalInfo(userPrincipal.getMemberId(), request, profileImage);

            AddInfoResponseDto response = new AddInfoResponseDto("추가 정보가 성공적으로 저장되었습니다");
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.CREATED, response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("Invalid JSON in 'data': {}", dataJson, e);
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            String key = e.getMessage();

            if("NICKNAME_REQUIRED".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_REQUIRED);
            } else if ("MEMBER_NOT_FOUND".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.MEMBER_NOT_FOUND);
            } else if ("NICKNAME_CONFLICT".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_CONFLICT);
            } else if ("NICKNAME_FORMAT_INVALID".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_FORMAT_INVALID);
            }
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "닉네임 중복 체크",
            description = "닉네임 사용 가능 여부를 확인합니다. 대소문자/양끝 공백을 무시하여 비교합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검사 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me/nicknameCheck")
    public ResponseEntity<com.runnity.global.response.ApiResponse<NicknameCheckResponseDto>> checkNickname(
            @RequestParam("nickname") String nickname,
            @AuthenticationPrincipal UserPrincipal principal // 자신의 닉네임 허용 처리
    ) {
        try {
            NicknameCheckResponseDto result =
                    memberService.checkNicknameAvailability(nickname, principal != null ? principal.getMemberId() : null);
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.NICKNAME_CHECK_OK, result);
        } catch (IllegalArgumentException e) {
            String key = e.getMessage();

            if ("NICKNAME_REQUIRED".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_REQUIRED);
            } else if ("NICKNAME_FORMAT_INVALID".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_FORMAT_INVALID);
            } else if ("NICKNAME_CONFLICT".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_CONFLICT);
            }
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INVALID_INPUT);
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

            ProfileResponseDto profile = memberService.getProfile(userPrincipal.getMemberId());
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.PROFILE_FETCH_OK, profile);

        } catch (IllegalArgumentException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.NOT_FOUND);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "multipart/form-data로 data(JSON) + profileImage(File, 선택)을 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 정보 없음"),
            @ApiResponse(responseCode = "404", description = "회원 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping(
            value = "/me/profile",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<com.runnity.global.response.ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            if (userPrincipal == null) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.UNAUTHORIZED);
            }

            ProfileUpdateRequestDto request = objectMapper.readValue(dataJson, ProfileUpdateRequestDto.class);

            // 서비스 호출
            memberService.updateProfile(userPrincipal.getMemberId(), request, profileImage);

            return com.runnity.global.response.ApiResponse.success(SuccessStatus.PROFILE_UPDATE_OK, null);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("Invalid JSON in 'data': {}", dataJson, e);
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            String key = e.getMessage();
            if("NICKNAME_REQUIRED".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_REQUIRED);
            } else if ("MEMBER_NOT_FOUND".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.MEMBER_NOT_FOUND);
            } else if ("NICKNAME_CONFLICT".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_CONFLICT);
            } else if ("NICKNAME_FORMAT_INVALID".equals(key)){
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.NICKNAME_FORMAT_INVALID);
            }
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.MEMBER_NOT_FOUND);
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
            TokenResponseDto resp = memberService.refreshAccessToken(request.getRefreshToken());
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK, resp);
        } catch (IllegalArgumentException e) {
            String key = e.getMessage();
            if ("MEMBER_NOT_FOUND".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.MEMBER_NOT_FOUND);
            } else if ("INVALID_TOKEN".equals(key)) {
                return com.runnity.global.response.ApiResponse.error(ErrorStatus.INVALID_TOKEN);
            }
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
            memberService.logout(userPrincipal.getMemberId(), accessToken);
            return com.runnity.global.response.ApiResponse.success(SuccessStatus.OK);
        } catch (Exception e) {
            return com.runnity.global.response.ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/images/profile/{memberId}")
    @Operation(
            summary = "프로필 이미지 조회 (프록시)",
            description = "S3 Private 버킷에서 프로필 이미지를 중계합니다. " +
                    "프로필 조회 API에서 반환된 profileImageUrl을 브라우저가 호출하면 " +
                    "백엔드가 S3에서 이미지를 가져와 반환합니다. " +
                    "이미지는 1시간 동안 브라우저에 캐싱됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이미지 반환 성공",
                    content = @Content(mediaType = "image/jpeg")
            ),
            @ApiResponse(responseCode = "404", description = "이미지 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Resource> getProfileImage(@PathVariable Long memberId) {
        try {
            // 1. DB에서 S3 키 조회
            String s3Key = memberService.getProfileImageKey(memberId);

            if (s3Key == null || s3Key.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            // 2. S3에서 파일 다운로드
            S3Object s3Object = amazonS3.getObject(bucket, s3Key);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            // 3. Content-Type 결정
            String contentType = determineContentType(s3Key);

            // 4. 응답 반환 (1시간 캐싱)
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(new InputStreamResource(inputStream));

        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return ResponseEntity.notFound().build();
            }
            log.error("S3 에러: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("이미지 조회 실패: memberId={}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineContentType(String s3Key) {
        String lower = s3Key.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
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