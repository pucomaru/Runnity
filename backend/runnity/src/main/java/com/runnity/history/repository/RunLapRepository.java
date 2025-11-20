package com.runnity.history.repository;

import com.runnity.history.domain.RunLap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RunLapRepository extends JpaRepository<RunLap, Long> {

    List<RunLap> findByRunRecord_RunRecordIdAndIsDeletedFalseOrderBySequence(Long runRecordId);

}
