package com.tutorialregister.service;

import com.tutorialregister.dto.AssessmentRequest;
import com.tutorialregister.dto.AssessmentResponse;
import com.tutorialregister.model.Assessment;
import com.tutorialregister.model.AssessmentType;
import com.tutorialregister.repository.AssessmentRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentService studentService;
    private final StaffService staffService;
    private final UserAccountService userAccountService;

    public AssessmentService(
        AssessmentRepository assessmentRepository,
        StudentService studentService,
        StaffService staffService,
        UserAccountService userAccountService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.studentService = studentService;
        this.staffService = staffService;
        this.userAccountService = userAccountService;
    }

    public List<AssessmentResponse> findAll() {
        String username = userAccountService.getCurrentUsername();
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        Long instId = currentUser != null && currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;

        if (userAccountService.hasRole("ADMIN")) {
            return instId == null
                ? assessmentRepository.findAll().stream().map(this::toResponse).toList()
                : assessmentRepository.findByStudentInstitutionId(instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STAFF")) {
            return instId == null
                ? List.of()
                : assessmentRepository.findAssessmentsForStaff(username, instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STUDENT")) {
            return assessmentRepository.findByStudentUserAccountUsername(username).stream().map(this::toResponse).toList();
        }
        return List.of();
    }

    public AssessmentResponse findById(Long id) {
        Assessment assessment = getAssessment(id);
        checkAssessmentAccess(assessment);
        return toResponse(assessment);
    }

    public AssessmentResponse create(AssessmentRequest request) {
        // Access control on the student is handled automatically by studentService.getStudent() in applyRequest
        Assessment assessment = new Assessment();
        applyRequest(assessment, request);
        return toResponse(assessmentRepository.save(assessment));
    }

    public AssessmentResponse update(Long id, AssessmentRequest request) {
        Assessment assessment = getAssessment(id);
        checkAssessmentAccess(assessment);
        // Student access checked by studentService.getStudent() in applyRequest
        applyRequest(assessment, request);
        return toResponse(assessmentRepository.save(assessment));
    }

    public void delete(Long id) {
        Assessment assessment = getAssessment(id);
        checkAssessmentAccess(assessment);
        assessmentRepository.delete(assessment);
    }

    private Assessment getAssessment(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
        checkAssessmentAccess(assessment);
        return assessment;
    }

    private void checkAssessmentAccess(Assessment assessment) {
        if (assessment.getStudent() != null) {
            userAccountService.verifyInstitution(assessment.getStudent().getInstitution());
        }
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            boolean isAssigned = (assessment.getEvaluatedBy() != null && assessment.getEvaluatedBy().getUserAccount() != null && username.equals(assessment.getEvaluatedBy().getUserAccount().getUsername()))
                || assessment.getStudent().getEnrolledCourses().stream()
                    .anyMatch(c -> c.getTeacher() != null && c.getTeacher().getUserAccount() != null && username.equals(c.getTeacher().getUserAccount().getUsername()));
            if (!isAssigned) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this assessment record");
            }
        } else if (userAccountService.hasRole("STUDENT")) {
            if (assessment.getStudent() == null || assessment.getStudent().getUserAccount() == null || !username.equals(assessment.getStudent().getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this assessment record");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        return new AssessmentResponse(
            assessment.getId(),
            studentService.toSummary(assessment.getStudent()),
            staffService.toSummary(assessment.getEvaluatedBy()),
            assessment.getTitle(),
            assessment.getType(),
            assessment.getMaxMarks(),
            assessment.getMarksObtained(),
            assessment.getAssessmentDate(),
            assessment.getRemarks()
        );
    }

    private void applyRequest(Assessment assessment, AssessmentRequest request) {
        com.tutorialregister.model.Student student = studentService.getStudent(request.studentId());
        userAccountService.verifyInstitution(student.getInstitution());
        assessment.setStudent(student);

        if (request.evaluatedById() != null) {
            com.tutorialregister.model.Staff evaluator = staffService.getStaff(request.evaluatedById());
            userAccountService.verifyInstitution(evaluator.getInstitution());
            assessment.setEvaluatedBy(evaluator);
        } else {
            assessment.setEvaluatedBy(null);
        }

        assessment.setTitle(request.title());
        assessment.setType(request.type() == null ? AssessmentType.TEST : request.type());
        assessment.setMaxMarks(request.maxMarks());
        assessment.setMarksObtained(request.marksObtained());
        assessment.setAssessmentDate(request.assessmentDate());
        assessment.setRemarks(request.remarks());
    }
}
