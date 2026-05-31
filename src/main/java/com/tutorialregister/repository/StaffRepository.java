package com.tutorialregister.repository;

import com.tutorialregister.model.Staff;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserAccountUsername(String username);
    java.util.List<Staff> findByInstitutionId(Long institutionId);
}
