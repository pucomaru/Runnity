package com.runnity.notification.domain;

import com.runnity.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_outbox_id")
    private Long notificationOutboxId;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Builder
    public NotificationOutbox(Long challengeId, NotificationType type) {
        this.challengeId = challengeId;
        this.type = type;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
    }

    public enum NotificationType {
        CHALLENGE_READY,
        CHALLENGE_DONE
    }

    public enum OutboxStatus {
        PENDING,
        SENT,
        FAILED
    }
}

