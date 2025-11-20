package com.runnity.websocket.enums;

/**
 * Redis Pub/Sub 이벤트 타입
 * 
 * WebSocket 서버 간 이벤트 동기화에 사용되는 이벤트 타입을 정의
 */
public enum RedisEventType {
    /**
     * 사용자 입장
     */
    USER_ENTERED,
    
    /**
     * 사용자 퇴장
     */
    USER_LEFT,
    
    /**
     * 참가자 정보 업데이트
     */
    PARTICIPANT_UPDATE
}

