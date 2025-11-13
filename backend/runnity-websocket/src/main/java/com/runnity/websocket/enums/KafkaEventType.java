package com.runnity.websocket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum KafkaEventType {
    /**
     * 챌린지 시작 (첫 입장 시)
     */
    START("start"),
    
    /**
     * 러닝 중 (주기적 RECORD 처리)
     */
    RUNNING("running"),
    
    /**
     * 완주
     */
    FINISH("finish"),
    
    /**
     * 퇴장 (reason 필드 필수)
     */
    LEAVE("leave");
    
    private final String value;
    
    KafkaEventType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static KafkaEventType fromValue(String value) {
        for (KafkaEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 KafkaEventType: " + value);
    }
}

