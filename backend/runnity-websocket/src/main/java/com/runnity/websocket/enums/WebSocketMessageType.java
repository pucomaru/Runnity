package com.runnity.websocket.enums;

/**
 * WebSocket 메시지 타입
 * 
 * 클라이언트-서버 간 WebSocket 통신에 사용되는 메시지 타입을 정의
 */
public enum WebSocketMessageType {
    // ========== 서버 → 클라이언트 ==========
    /**
     * 연결 성공
     */
    CONNECTED,
    
    /**
     * 다른 참가자 입장
     */
    USER_ENTERED,
    
    /**
     * 다른 참가자 퇴장
     */
    USER_LEFT,
    
    /**
     * 참가자 정보 업데이트
     */
    PARTICIPANT_UPDATE,
    
    /**
     * 오류 메시지
     */
    ERROR,
    
    // ========== 클라이언트 → 서버 ==========
    /**
     * 러닝 기록 (주기적)
     */
    RECORD,
    
    /**
     * 자발적 포기
     */
    QUIT,
    
    /**
     * 연결 유지 확인 (클라이언트 → 서버)
     */
    PING,
    
    /**
     * 연결 유지 응답 (서버 → 클라이언트)
     */
    PONG,
    
    /**
     * 강제 퇴장 요청 (클라이언트 → 서버)
     */
    KICKED
}
