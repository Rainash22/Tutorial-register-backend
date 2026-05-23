package com.tutorialregister.repository;

import com.tutorialregister.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
}
