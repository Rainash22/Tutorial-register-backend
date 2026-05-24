package com.tutorialregister.repository;

import com.tutorialregister.model.ClassSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    List<ClassSchedule> findByCourseId(Long courseId);
}
