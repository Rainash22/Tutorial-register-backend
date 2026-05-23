package com.tutorialregister.service;

import com.tutorialregister.dto.StudentRequest;
import com.tutorialregister.dto.StudentResponse;
import com.tutorialregister.dto.StudentSummaryResponse;
import com.tutorialregister.model.Student;
import com.tutorialregister.model.StudentStatus;
import com.tutorialregister.repository.StudentRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StaffService staffService;
    private final UserAccountService userAccountService;

    public StudentService(
        StudentRepository studentRepository,
        StaffService staffService,
        UserAccountService userAccountService
    ) {
        this.studentRepository = studentRepository;
        this.staffService = staffService;
        this.userAccountService = userAccountService;
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public StudentResponse findById(Long id) {
        return toResponse(getStudent(id));
    }

    public StudentResponse create(StudentRequest request) {
        Student student = new Student();
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudent(id);
        applyRequest(student, request);
        return toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        Student student = getStudent(id);
        studentRepository.delete(student);
    }

    Student getStudent(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student", id));
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
            student.getCourseName(),
            student.getGuardianName(),
            student.getGuardianPhone(),
            student.getEmail(),
            student.getPhone(),
            student.getAddress(),
            student.getAdmissionDate(),
            student.getStatus(),
            staffService.toSummary(student.getAssignedStaff()),
            userAccountService.toSummary(student.getUserAccount())
        );
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setFullName(request.fullName());
        student.setAdmissionNumber(request.admissionNumber());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setClassName(request.className());
        student.setCourseName(request.courseName());
        student.setGuardianName(request.guardianName());
        student.setGuardianPhone(request.guardianPhone());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setAddress(request.address());
        student.setAdmissionDate(request.admissionDate());
        student.setStatus(request.status() == null ? StudentStatus.ACTIVE : request.status());
        student.setAssignedStaff(request.assignedStaffId() == null ? null : staffService.getStaff(request.assignedStaffId()));
        student.setUserAccount(request.userAccountId() == null ? null : userAccountService.getUserAccount(request.userAccountId()));
    }
}
