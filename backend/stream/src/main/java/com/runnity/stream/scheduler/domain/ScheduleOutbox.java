package com.runnity.stream.scheduler.domain;

import com.runnity.stream.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedule_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_outbox_id")
    private Long scheduleOutboxId;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ScheduleEventType eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Builder
    public ScheduleOutbox(Long challengeId, ScheduleEventType eventType, String payload) {
        this.challengeId = challengeId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
    }

    /**
     * 스케줄 이벤트 타입
     */
    public enum ScheduleEventType {
        SCHEDULE_CREATE("스케줄 생성"),
        SCHEDULE_DELETE("스케줄 삭제");

        private final String description;

        ScheduleEventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Outbox 처리 상태
     */
    public enum OutboxStatus {
        PENDING("대기 중"),
        PUBLISHED("발행 완료"),
        FAILED("실패");

        private final String description;

        OutboxStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

