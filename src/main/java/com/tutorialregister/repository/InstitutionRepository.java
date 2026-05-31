package com.tutorialregister.repository;

import com.tutorialregister.model.Institution;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByCodeIgnoreCase(String code);
}
