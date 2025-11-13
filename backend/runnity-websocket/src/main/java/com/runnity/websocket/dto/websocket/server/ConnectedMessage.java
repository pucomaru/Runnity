package com.runnity.websocket.dto.websocket.server;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.Set;

/**
 * CONNECTED 메시지 (서버 → 클라이언트)
 * 
 * WebSocket 연결 성공 시 전송되는 메시지입니다.
 * 현재 참여 중인 다른 참가자 목록을 함께 전송합니다.
 * 
 * @param type 메시지 타입 ("CONNECTED")
 * @param challengeId 챌린지 ID (디버깅 및 검증용)
 * @param userId 사용자 ID (디버깅 및 클라이언트 상태 확인용)
 * @param participants 현재 참여 중인 다른 참가자 목록 (본인 제외)
 * @param timestamp 타임스탬프
 */
public record ConnectedMessage(
    @NotNull String type,
    @NotNull Long challengeId,
    @NotNull Long userId,
    @NotNull List<Participant> participants,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public ConnectedMessage {
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        Set<ConstraintViolation<ConnectedMessage>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.iterator().next().getMessage());
        }
    }
    
    public ConnectedMessage(Long challengeId, Long userId, List<Participant> participants) {
        this("CONNECTED", challengeId, userId, participants, System.currentTimeMillis());
    }
    
    /**
     * 참가자 정보
     * 
     * @param userId 사용자 ID
     * @param nickname 닉네임
     * @param profileImage 프로필 이미지 URL
     * @param distance 현재 거리 (km)
     * @param pace 현재 페이스 (분/km)
     */
    public record Participant(
        @NotNull Long userId,
        @NotNull String nickname,
        String profileImage,
        @NotNull @PositiveOrZero Double distance,
        @NotNull @PositiveOrZero Double pace
    ) {
        private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        
        public Participant {
            Set<ConstraintViolation<Participant>> violations = validator.validate(this);
            if (!violations.isEmpty()) {
                throw new IllegalArgumentException(violations.iterator().next().getMessage());
            }
        }
    }
}

