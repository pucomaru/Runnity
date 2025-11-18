package com.runnity.broadcast.controller;

import com.runnity.broadcast.dto.BroadcastDto;
import com.runnity.broadcast.dto.BroadcastJoinResponse;
import com.runnity.broadcast.service.BroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 중계 관련 API 컨트롤러
 */
@Tag(name = "Broadcast", description = "중계 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/broadcast")
public class BroadcastController {

    private final BroadcastService broadcastService;

    @Operation(
            summary = "활성화된 중계방 목록 조회",
            description = "현재 활성화된 모든 중계방 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중계방 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = BroadcastDto.class))
                    )
            }
    )
    @GetMapping("/active")
    public ResponseEntity<List<BroadcastDto>> getActiveBroadcasts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "distance", required = false) List<String> distance, // ONE,TWO 반복키 방식
            @RequestParam(value = "sort", required = false, defaultValue = "LATEST") String sort
    ) {
        return ResponseEntity.ok(broadcastService.getActiveBroadcasts(keyword, distance, sort));
    }

    @Operation(
            summary = "중계방 입장",
            description = "중계방 입장 시 WebSocket 연결 정보(wsUrl, topic)를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "입장 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BroadcastJoinResponse.class),
                                    examples = @ExampleObject(
                                            name = "join-success-example",
                                            value = """
                    {
                      "wsUrl": "ws://43.203.250.119:8080/ws",
                      "topic": "/topic/broadcast/13",
                      "challengeId": 13
                    }
                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/join")
    public ResponseEntity<BroadcastJoinResponse> joinBroadcast(
            @RequestParam Long challengeId
    ) {
        return ResponseEntity.ok(
                broadcastService.joinBroadcast(challengeId)
        );
    }
}
