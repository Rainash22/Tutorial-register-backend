package com.tutorialregister.service;

import com.tutorialregister.model.Course;
import com.tutorialregister.model.Institution;
import com.tutorialregister.model.Staff;
import com.tutorialregister.model.Student;
import com.tutorialregister.model.UserAccount;
import com.tutorialregister.repository.CourseRepository;
import com.tutorialregister.repository.InstitutionRepository;
import com.tutorialregister.repository.StaffRepository;
import com.tutorialregister.repository.StudentRepository;
import com.tutorialregister.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final CourseRepository courseRepository;

    public InstitutionService(
        InstitutionRepository institutionRepository,
        UserAccountRepository userAccountRepository,
        StudentRepository studentRepository,
        StaffRepository staffRepository,
        CourseRepository courseRepository
    ) {
        this.institutionRepository = institutionRepository;
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.courseRepository = courseRepository;
    }

    @jakarta.annotation.PostConstruct
    public void seedAndMigrate() {
        Institution defaultInstitution = institutionRepository.findByCodeIgnoreCase("INST01")
            .orElseGet(() -> {
                Institution inst = new Institution();
                inst.setName("Default Institution");
                inst.setCode("INST01");
                return institutionRepository.save(inst);
            });

        // Migrate UserAccounts
        for (UserAccount user : userAccountRepository.findAll()) {
            if (user.getInstitution() == null) {
                user.setInstitution(defaultInstitution);
                userAccountRepository.save(user);
            }
        }

        // Migrate Students
        for (Student student : studentRepository.findAll()) {
            if (student.getInstitution() == null) {
                student.setInstitution(defaultInstitution);
                studentRepository.save(student);
            }
        }

        // Migrate Staff
        for (Staff staff : staffRepository.findAll()) {
            if (staff.getInstitution() == null) {
                staff.setInstitution(defaultInstitution);
                staffRepository.save(staff);
            }
        }

        // Migrate Courses
        for (Course course : courseRepository.findAll()) {
            if (course.getInstitution() == null) {
                course.setInstitution(defaultInstitution);
                courseRepository.save(course);
            }
        }
    }
}
