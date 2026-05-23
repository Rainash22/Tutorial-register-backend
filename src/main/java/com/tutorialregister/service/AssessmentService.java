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

    public AssessmentService(
        AssessmentRepository assessmentRepository,
        StudentService studentService,
        StaffService staffService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.studentService = studentService;
        this.staffService = staffService;
    }

    public List<AssessmentResponse> findAll() {
        return assessmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AssessmentResponse findById(Long id) {
        return toResponse(getAssessment(id));
    }

    public AssessmentResponse create(AssessmentRequest request) {
        Assessment assessment = new Assessment();
        applyRequest(assessment, request);
        return toResponse(assessmentRepository.save(assessment));
    }

    public AssessmentResponse update(Long id, AssessmentRequest request) {
        Assessment assessment = getAssessment(id);
        applyRequest(assessment, request);
        return toResponse(assessmentRepository.save(assessment));
    }

    public void delete(Long id) {
        Assessment assessment = getAssessment(id);
        assessmentRepository.delete(assessment);
    }

    private Assessment getAssessment(Long id) {
        return assessmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
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
        assessment.setStudent(studentService.getStudent(request.studentId()));
        assessment.setEvaluatedBy(request.evaluatedById() == null ? null : staffService.getStaff(request.evaluatedById()));
        assessment.setTitle(request.title());
        assessment.setType(request.type() == null ? AssessmentType.TEST : request.type());
        assessment.setMaxMarks(request.maxMarks());
        assessment.setMarksObtained(request.marksObtained());
        assessment.setAssessmentDate(request.assessmentDate());
        assessment.setRemarks(request.remarks());
    }
}
