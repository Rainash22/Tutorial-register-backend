package com.tutorialregister.repository;

import com.tutorialregister.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeRepository extends JpaRepository<Fee, Long> {
}
