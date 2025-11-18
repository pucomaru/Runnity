package com.runnity.websocket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeaveReason {
    /**
     * 자발적 포기
     */
    QUIT("QUIT", false),
    
    /**
     * 무응답 타임아웃 (일정 시간 동안 RECORD 없음)
     */
    TIMEOUT("TIMEOUT", true),
    
    /**
     * 연결 끊김 (WebSocket 연결 비정상 종료)
     */
    DISCONNECTED("DISCONNECTED", true),
    
    /**
     * 오류 발생
     */
    ERROR("ERROR", true),
    
    /**
     * 강제 퇴장 (부정 행위 감지 등)
     */
    KICKED("KICKED", false),
    
    /**
     * 시간 만료 (챌린지 제한 시간 초과)
     */
    EXPIRED("EXPIRED", false),
    
    /**
     * 완주
     */
    FINISH("FINISH", false);
    
    private final String value;
    private final boolean reconnectable;
    
    LeaveReason(String value, boolean reconnectable) {
        this.value = value;
        this.reconnectable = reconnectable;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public boolean isReconnectable() {
        return reconnectable;
    }
    
    @JsonCreator
    public static LeaveReason fromValue(String value) {
        for (LeaveReason reason : values()) {
            if (reason.value.equals(value)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 LeaveReason: " + value);
    }
}

