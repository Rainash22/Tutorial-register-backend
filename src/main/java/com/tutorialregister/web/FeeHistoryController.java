package com.tutorialregister.web;

import com.tutorialregister.dto.FeeHistoryRequest;
import com.tutorialregister.dto.FeeHistoryResponse;
import com.tutorialregister.service.FeeHistoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fee-history")
public class FeeHistoryController {

    private final FeeHistoryService feeHistoryService;

    public FeeHistoryController(FeeHistoryService feeHistoryService) {
        this.feeHistoryService = feeHistoryService;
    }

    /** List all fee history / installment records. */
    @GetMapping
    public List<FeeHistoryResponse> findAll() {
        return feeHistoryService.findAll();
    }

    /** Get a single fee history record by ID. */
    @GetMapping("/{id}")
    public FeeHistoryResponse findById(@PathVariable Long id) {
        return feeHistoryService.findById(id);
    }

    /**
     * Get all installments recorded against a specific fee.
     * Use this to show the payment timeline for a student-course fee record.
     */
    @GetMapping("/fee/{feeId}")
    public List<FeeHistoryResponse> findByFee(@PathVariable Long feeId) {
        return feeHistoryService.findByFee(feeId);
    }

    /**
     * Get the full payment audit trail for a student across all courses.
     */
    @GetMapping("/student/{studentId}")
    public List<FeeHistoryResponse> findByStudent(@PathVariable Long studentId) {
        return feeHistoryService.findByStudent(studentId);
    }

    /**
     * Record a new payment installment.
     * This automatically reduces the parent fee's outstanding balance and
     * updates the fee status (PARTIAL or PAID).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeHistoryResponse create(@Valid @RequestBody FeeHistoryRequest request) {
        return feeHistoryService.create(request);
    }

    /**
     * Delete (reverse) a payment installment.
     * This restores the parent fee's outstanding balance by adding back the reversed amount.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        feeHistoryService.delete(id);
    }
}
