package com.tutorialregister.repository;

import com.tutorialregister.model.Assessment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("SELECT a FROM Assessment a WHERE a.student.institution.id = :institutionId AND (a.evaluatedBy.userAccount.username = :username OR a.student.id IN (SELECT DISTINCT s.id FROM Student s JOIN s.enrolledCourses c JOIN c.teachers t WHERE t.userAccount.username = :username))")
    List<Assessment> findAssessmentsForStaff(@Param("username") String username, @Param("institutionId") Long institutionId);

    List<Assessment> findByStudentUserAccountUsername(String username);

    List<Assessment> findByStudentInstitutionId(Long institutionId);
}
