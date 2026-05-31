package com.tutorialregister.repository;

import com.tutorialregister.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserAccountUsername(String username);
    List<Student> findByInstitutionId(Long institutionId);

    @Query("SELECT DISTINCT s FROM Student s JOIN s.enrolledCourses c JOIN c.teachers t WHERE s.institution.id = :institutionId AND t.userAccount.username = :username")
    List<Student> findStudentsForStaff(@Param("username") String username, @Param("institutionId") Long institutionId);
}
