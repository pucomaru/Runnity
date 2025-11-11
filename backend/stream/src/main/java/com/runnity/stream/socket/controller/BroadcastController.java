package com.runnity.stream.socket.controller;

import com.runnity.stream.socket.BroadcastSessionService;
import com.runnity.stream.socket.dto.BroadcastResponse;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 현재 Redis에 저장된 활성 방송 목록 조회용 REST API
 * - 프론트나 외부 서비스에서 현재 방송 리스트 확인 가능
 * - 로컬 테스트용 사용 가능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/broadcast")
public class BroadcastController {

    private final BroadcastSessionService broadcastSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    // 현재 방송 중인 챌린지 목록 조회
    @GetMapping("/active")
    public ResponseEntity<List<BroadcastResponse>> getActiveBroadcasts() {
        return ResponseEntity.ok(broadcastSessionService.getActiveBroadcasts());
    }

    @PostMapping("/{challengeId}")
    public void sendMockFrame(@PathVariable Long challengeId) {
        ChallengeStreamMessage mock = ChallengeStreamMessage.builder()
                .eventType("running")
                .challengeId(challengeId)
                .runnerId(777L)
                .distance(3210.5)
                .pace(5.2)
                .ranking(1)
                .isBroadcast(true)
                .reason(null)
                .build();

        messagingTemplate.convertAndSend("/topic/broadcast/" + challengeId, mock);
        log.info("Mock 프레임 송출 완료: {}", mock);
    }

}
