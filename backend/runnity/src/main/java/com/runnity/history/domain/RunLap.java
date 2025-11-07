package com.runnity.history.domain;

import com.runnity.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "run_lap")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunLap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_lap_id")
    private Long runLapId;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private Float distance;  // 구간 거리 (km)

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(nullable = false)
    private Float pace;

    @Column(nullable = false)
    private Integer bpm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_record_id", nullable = false, foreignKey = @ForeignKey(name = "fk_run_lap_run_record"))
    private RunRecord runRecord;
}
