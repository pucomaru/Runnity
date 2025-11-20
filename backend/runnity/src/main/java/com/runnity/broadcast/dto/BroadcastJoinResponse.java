package com.runnity.broadcast.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "중계방 입장 응답 dto")
public class BroadcastJoinResponse {

    @Schema(
            description = "WebSocket 접속 URL",
            example = "ws://43...:8080/ws"
    )
    private String wsUrl;

    @Schema(
            description = "구독해야 할 STOMP topic",
            example = "/topic/broadcast/13"
    )
    private String topic;

    @Schema(
            description = "입장한 챌린지 ID",
            example = "13"
    )
    private Long challengeId;
}
