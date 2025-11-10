package com.runnity.stream.socket.controller;

import com.runnity.stream.socket.BroadcastService;
import com.runnity.stream.socket.dto.BroadcastResponse;
import com.runnity.stream.socket.dto.ChallengeRecordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 현재 Redis에 저장된 활성 방송 목록 조회용 REST API
 * - 프론트나 외부 서비스에서 현재 방송 리스트 확인 가능
 * - 로컬 테스트용 사용 가능
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/broadcast")
public class BroadcastController {

    private final BroadcastService broadcastService;

    // 현재 방송 중인 챌린지 목록 조회
    @GetMapping("/active")
    public ResponseEntity<List<BroadcastResponse>> getActiveBroadcasts() {
        return ResponseEntity.ok(broadcastService.getActiveBroadcasts());
    }

    @PostMapping("/mock/{challengeId}")
    public void sendMock(@PathVariable Long challengeId){
        ChallengeRecordMessage
    }

}
