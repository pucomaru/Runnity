package com.runnity.stream.socket.controller;

import com.runnity.stream.socket.dto.BroadcastResponse;
import com.runnity.stream.socket.dto.ChallengeStreamMessage;
import com.runnity.stream.socket.dto.WebSocketPayloadWrapper;
import com.runnity.stream.socket.util.BroadcastRedisUtil;
import com.runnity.stream.socket.util.ChallengeMetaRedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


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

    private final BroadcastRedisUtil redisUtil;
    private final ChallengeMetaRedisUtil metaRedisUtil;
    private final SimpMessagingTemplate messagingTemplate;

    // 현재 방송 중인 챌린지 목록 조회
    @GetMapping("/active")
    public ResponseEntity<List<BroadcastResponse>> getActiveBroadcasts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "distance", required = false) List<String> distanceCodes,
            @RequestParam(value = "sort", required = false, defaultValue = "POPULAR") String sort
    ) {

        Set<String> ids = redisUtil.getActiveChallengeIds();
        List<BroadcastResponse> result = new ArrayList<>();

        for (String idStr : ids) {
            try {
                Long challengeId = Long.parseLong(idStr);

                Map<String, String> meta = metaRedisUtil.getMeta(challengeId);
                if (meta.isEmpty()) continue;

                String title = meta.getOrDefault("title", "제목 없음");
                String distanceRaw = meta.getOrDefault("distance", "UNKNOWN");
                String distanceCode = normalize(distanceRaw);

                int participantCount = Integer.parseInt(
                        meta.getOrDefault("actualParticipantCount", "0")
                );

                int viewerCount = redisUtil.getViewerCount(challengeId);
                String createdAt = redisUtil.getCreatedAt(challengeId);

                result.add(new BroadcastResponse(
                        challengeId,
                        title,
                        viewerCount,
                        participantCount,
                        createdAt,
                        distanceCode
                ));

            } catch (Exception e) {
                log.error("active 목록 조회 오류: {}", e.getMessage());
            }
        }

        // keyword 필터
        if (keyword != null && !keyword.isBlank()) {
            result = result.stream()
                    .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        // distance 필터
        if (distanceCodes != null && !distanceCodes.isEmpty()) {
            result = result.stream()
                    .filter(r -> distanceCodes.contains(r.getDistance()))
                    .toList();
        }

        // 정렬
        if ("POPULAR".equalsIgnoreCase(sort)) {
            result.sort((a, b) -> b.getViewerCount() - a.getViewerCount());
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            result.sort((a, b) -> {
                try {
                    return LocalDateTime.parse(b.getCreatedAt(), fmt)
                            .compareTo(LocalDateTime.parse(a.getCreatedAt(), fmt));
                } catch (Exception e) {
                    return 0;
                }
            });
        }

        return ResponseEntity.ok(result);
    }

    private String normalize(String raw) {
        return switch (raw) {
            case "0.1" -> "M100";
            case "0.5" -> "M500";
            case "1" -> "ONE";
            case "2" -> "TWO";
            case "3" -> "THREE";
            case "4" -> "FOUR";
            case "5" -> "FIVE";
            default -> raw;
        };
     }

    /** 테스트용 목업 프레임 송출 **/
    @PostMapping("/{challengeId}")
    public void sendMockFrame(@PathVariable Long challengeId) {

        ChallengeStreamMessage mock = ChallengeStreamMessage.builder()
                .eventType("running")
                .challengeId(challengeId)
                .runnerId(777L)
                .distance(1234.5)
                .pace(5.0)
                .ranking(1)
                .build();

        WebSocketPayloadWrapper wrapper = WebSocketPayloadWrapper.builder()
                .type("STREAM")
                .subtype("RUNNING")
                .challengeId(challengeId)
                .timestamp(System.currentTimeMillis())
                .payload(mock)
                .build();

        messagingTemplate.convertAndSend("/topic/broadcast/" + challengeId, wrapper);

        log.info("Mock 프레임 송출: {}", wrapper);
    }

}
