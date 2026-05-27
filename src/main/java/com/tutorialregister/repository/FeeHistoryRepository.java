package com.tutorialregister.repository;

import com.tutorialregister.model.FeeHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeHistoryRepository extends JpaRepository<FeeHistory, Long> {

    /** All installments recorded against a specific fee record. */
    List<FeeHistory> findByFeeId(Long feeId);

    /** Full payment audit trail for a specific student across all courses. */
    List<FeeHistory> findByStudentId(Long studentId);
}
