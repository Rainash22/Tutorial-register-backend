package com.tutorialregister.web;

import com.tutorialregister.dto.AssessmentRequest;
import com.tutorialregister.dto.AssessmentResponse;
import com.tutorialregister.service.AssessmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public List<AssessmentResponse> findAll() {
        return assessmentService.findAll();
    }

    @GetMapping("/{id}")
    public AssessmentResponse findById(@PathVariable Long id) {
        return assessmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse create(@Valid @RequestBody AssessmentRequest request) {
        return assessmentService.create(request);
    }

    @PutMapping("/{id}")
    public AssessmentResponse update(@PathVariable Long id, @Valid @RequestBody AssessmentRequest request) {
        return assessmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assessmentService.delete(id);
    }
}
