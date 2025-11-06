package com.runnity.challenge.domain;

import com.runnity.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long challengeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeStatus status;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeDistance distance;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(length = 255, nullable = true)
    private String password;

    @Builder
    public Challenge(
            String title,
            Integer maxParticipants,
            LocalDateTime startAt,
            String description,
            ChallengeDistance distance,
            Boolean isPrivate,
            String password
    ) {
        this.title = title;
        this.status = ChallengeStatus.RECRUITING;
        this.maxParticipants = maxParticipants;
        this.description = description;
        this.distance = distance;
        this.startAt = startAt;
        this.endAt = startAt.plusMinutes(distance.durationMinutes());
        this.isPrivate = isPrivate;
        this.password = password;
    }
}
