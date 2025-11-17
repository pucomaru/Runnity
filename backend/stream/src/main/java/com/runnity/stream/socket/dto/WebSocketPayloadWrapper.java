package com.runnity.stream.socket.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketPayloadWrapper {

    private String type;        //  LLM
    private String subtype;     // RUNNING, START, FINISH, OVERTAKE, TOP3, LLM_MESSAGE 등
    private Long challengeId;
    private long timestamp;

    private Object payload;     // ChallengeStreamMessage or LLMMessage
}