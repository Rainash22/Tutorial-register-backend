package com.tutorialregister.service;

import com.tutorialregister.dto.StudentRequest;
import com.tutorialregister.dto.StudentResponse;
import com.tutorialregister.dto.StudentSummaryResponse;
import com.tutorialregister.model.Course;
import com.tutorialregister.model.Student;
import com.tutorialregister.model.StudentStatus;
import com.tutorialregister.repository.StudentRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StaffService staffService;
    private final UserAccountService userAccountService;
    private final CourseService courseService;

    public StudentService(
        StudentRepository studentRepository,
        StaffService staffService,
        UserAccountService userAccountService,
        @Lazy CourseService courseService  // @Lazy breaks the circular dependency
    ) {
        this.studentRepository = studentRepository;
        this.staffService = staffService;
        this.userAccountService = userAccountService;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        String username = userAccountService.getCurrentUsername();
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        Long instId = currentUser != null && currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;

        if (userAccountService.hasRole("ADMIN")) {
            return instId == null
                ? studentRepository.findAll().stream().map(this::toResponse).toList()
                : studentRepository.findByInstitutionId(instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STAFF")) {
            return instId == null
                ? List.of()
                : studentRepository.findStudentsForStaff(username, instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STUDENT")) {
            return studentRepository.findByUserAccountUsername(username).stream().map(this::toResponse).toList();
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        checkStudentAccess(student);
        return toResponse(student);
    }

    public StudentResponse create(StudentRequest request) {
        enforceAdmin();
        Student student = new Student();
        applyRequest(student, request);
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        if (currentUser != null) {
            student.setInstitution(currentUser.getInstitution());
        }
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        enforceAdmin();
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        userAccountService.verifyInstitution(student.getInstitution());
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        enforceAdmin();
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        userAccountService.verifyInstitution(student.getInstitution());
        studentRepository.delete(student);
    }

    private void enforceAdmin() {
        if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Admin role required");
        }
    }

    private void checkStudentAccess(Student student) {
        userAccountService.verifyInstitution(student.getInstitution());
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            boolean isEnrolled = student.getEnrolledCourses().stream()
                .anyMatch(c -> c.getTeachers().stream().anyMatch(t -> t.getUserAccount() != null && username.equals(t.getUserAccount().getUsername())));
            if (!isEnrolled) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to student record");
            }
        } else if (userAccountService.hasRole("STUDENT")) {
            if (student.getUserAccount() == null || !username.equals(student.getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to student record");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    Student getStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        checkStudentAccess(student);
        return student;
    }

    StudentSummaryResponse toSummary(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentSummaryResponse(student.getId(), student.getFullName(), student.getAdmissionNumber());
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getFullName(),
            student.getAdmissionNumber(),
            student.getDateOfBirth(),
            student.getGender(),
            student.getClassName(),
            student.getGuardianName(),
            student.getGuardianPhone(),
            student.getEmail(),
            student.getPhone(),
            student.getAddress(),
            student.getAdmissionDate(),
            student.getStatus(),
            staffService.toSummary(student.getAssignedStaff()),
            userAccountService.toSummary(student.getUserAccount()),
            student.getEnrolledCourses().stream().map(courseService::toSummary).toList()
        );
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setFullName(request.fullName());
        student.setAdmissionNumber(request.admissionNumber());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setClassName(request.className());
        student.setGuardianName(request.guardianName());
        student.setGuardianPhone(request.guardianPhone());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setAddress(request.address());
        student.setAdmissionDate(request.admissionDate());
        student.setStatus(request.status() == null ? StudentStatus.ACTIVE : request.status());
        student.setAssignedStaff(request.assignedStaffId() == null ? null : staffService.getStaff(request.assignedStaffId()));
        student.setUserAccount(request.userAccountId() == null ? null : userAccountService.getUserAccount(request.userAccountId()));

        // Update enrolled courses from the supplied course IDs
        if (request.courseIds() != null) {
            Set<Course> courses = new HashSet<>();
            for (Long courseId : request.courseIds()) {
                courses.add(courseService.getCourse(courseId));
            }
            student.setEnrolledCourses(courses);
        }
    }
}

