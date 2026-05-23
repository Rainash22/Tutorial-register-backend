package com.tutorialregister.repository;

import com.tutorialregister.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
}
