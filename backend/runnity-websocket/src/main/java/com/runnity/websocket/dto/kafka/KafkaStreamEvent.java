package com.runnity.websocket.dto.kafka;

import com.runnity.websocket.enums.KafkaEventType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Set;

/**
 * Kafka 스트림 이벤트
 * 
 * Stream 서버로 전송되는 브로드캐스트 이벤트
 * 
 * @param eventType 이벤트 타입 (START, RUNNING, FINISH, LEAVE)
 * @param challengeId 챌린지 ID
 * @param runnerId 러너 ID (Member PK)
 * @param nickname 러너 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param distance 현재 누적 거리 (km)
 * @param pace 현재 페이스 (분/km)
 * @param ranking 현재 순위
 * @param isBroadcast 브로드캐스트 여부
 * @param reason 퇴장 사유 (leave일 때만 사용)
 * @param timestamp 이벤트 발생 시각
 */
public record KafkaStreamEvent(
    @NotNull KafkaEventType eventType,
    @NotNull Long challengeId,
    @NotNull Long runnerId,
    String nickname,
    String profileImage,
    @PositiveOrZero Double distance,
    @PositiveOrZero Integer pace,
    Integer ranking,
    Boolean isBroadcast,
    String reason,
    @NotNull Long timestamp
) {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public KafkaStreamEvent {
        Set<ConstraintViolation<KafkaStreamEvent>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                violations.iterator().next().getMessage()
            );
        }
    }
}

