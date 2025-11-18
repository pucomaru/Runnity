package com.runnity.challenge.controller;

import com.runnity.challenge.request.AdminChallengeCreateRequest;
import com.runnity.challenge.response.ChallengeResponse;
import com.runnity.challenge.service.ChallengeService;
import com.runnity.global.status.SuccessStatus;
import com.runnity.member.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/challenges")
@RequiredArgsConstructor
@Tag(name = "Admin Challenge", description = "어드민 챌린지 API")
public class AdminChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "인원 제한 없는 챌린지 생성", description = "인원 제한 검증 없이 챌린지를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeResponse>> createUnlimitedChallenge(
            @Valid @RequestBody AdminChallengeCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getMemberId();
        ChallengeResponse response = challengeService.createChallenge(request.toChallengeCreateRequest(), memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.CHALLENGE_CREATED,
                response
        );
    }
}

