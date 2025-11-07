package com.runnity.history.domain;

import com.runnity.global.domain.BaseEntity;
import com.runnity.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "run_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_record_id")
    private Long runRecordId;

    @Column(nullable = false)
    private Float distance;  // 거리 (km)

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;  // 소요 시간 (초)

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer pace;  // 페이스 (초/km)

    @Column(nullable = false)
    private Integer bpm;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false, length = 20)
    private RunRecordType runType;

    @Column(nullable = false)
    private Float calories;

    @Column(columnDefinition = "json")
    private String route;  // 경로 데이터 (JSON)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_run_record_member"))
    private Member member;
}