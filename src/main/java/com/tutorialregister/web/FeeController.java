package com.tutorialregister.web;

import com.tutorialregister.dto.FeeRequest;
import com.tutorialregister.dto.FeeResponse;
import com.tutorialregister.service.FeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    /** List all fee records. */
    @GetMapping
    public List<FeeResponse> findAll() {
        return feeService.findAll();
    }

    /** Get a single fee record by ID. */
    @GetMapping("/{id}")
    public FeeResponse findById(@PathVariable Long id) {
        return feeService.findById(id);
    }

    /** Get all fee records for a specific student (shows per-course outstanding balances). */
    @GetMapping("/student/{studentId}")
    public List<FeeResponse> findByStudent(@PathVariable Long studentId) {
        return feeService.findByStudent(studentId);
    }

    /** Get all fee records for a specific course. */
    @GetMapping("/course/{courseId}")
    public List<FeeResponse> findByCourse(@PathVariable Long courseId) {
        return feeService.findByCourse(courseId);
    }

    /**
     * Manually create a fee record (admin override).
     * Normally fees are auto-created when a student is enrolled via
     * POST /api/courses/{id}/enrol/{studentId}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeResponse create(@Valid @RequestBody FeeRequest request) {
        return feeService.create(request);
    }

    /** Update a fee record (e.g., adjust due date, status, or remarks). */
    @PutMapping("/{id}")
    public FeeResponse update(@PathVariable Long id, @Valid @RequestBody FeeRequest request) {
        return feeService.update(id, request);
    }

    /** Delete a fee record. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        feeService.delete(id);
    }

    /**
     * Set the same due date on ALL active (non-CANCELLED) fee records in one call.
     * Request body: { "dueDate": "2026-06-30" }
     * Response:     { "updatedCount": 42 }
     */
    @PatchMapping("/due-date")
    public Map<String, Integer> setDueDateForAll(@RequestBody @NotNull Map<String, LocalDate> body) {
        LocalDate dueDate = body.get("dueDate");
        if (dueDate == null) {
            throw new IllegalArgumentException("Request body must contain 'dueDate' (format: YYYY-MM-DD)");
        }
        int updatedCount = feeService.setDueDateForAll(dueDate);
        return Map.of("updatedCount", updatedCount);
    }
}
