package com.runnity.challenge.controller;

import com.runnity.challenge.dto.ChallengeCreateRequest;
import com.runnity.challenge.dto.ChallengeResponse;
import com.runnity.challenge.service.ChallengeService;
import com.runnity.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Tag(name = "Challenge", description = "챌린지 API")
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 부족"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<com.runnity.global.response.ApiResponse<ChallengeResponse>> createChallenge(
            @Valid @RequestBody ChallengeCreateRequest request
    ) {
        Long memberId = getCurrentUserId();
        ChallengeResponse response = challengeService.createChallenge(request, memberId);
        return com.runnity.global.response.ApiResponse.success(
                SuccessStatus.CHALLENGE_CREATED,
                response
        );
    }

    private Long getCurrentUserId() {
        // TODO: 추후 인증 로직이 추가되면 수정 필요
        return 1L;
    }
}
