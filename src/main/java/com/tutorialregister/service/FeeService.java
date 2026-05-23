package com.tutorialregister.service;

import com.tutorialregister.dto.FeeRequest;
import com.tutorialregister.dto.FeeResponse;
import com.tutorialregister.model.Fee;
import com.tutorialregister.model.FeeStatus;
import com.tutorialregister.repository.FeeRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentService studentService;

    public FeeService(FeeRepository feeRepository, StudentService studentService) {
        this.feeRepository = feeRepository;
        this.studentService = studentService;
    }

    public List<FeeResponse> findAll() {
        return feeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public FeeResponse findById(Long id) {
        return toResponse(getFee(id));
    }

    public FeeResponse create(FeeRequest request) {
        Fee fee = new Fee();
        applyRequest(fee, request);
        return toResponse(feeRepository.save(fee));
    }

    public FeeResponse update(Long id, FeeRequest request) {
        Fee fee = getFee(id);
        applyRequest(fee, request);
        return toResponse(feeRepository.save(fee));
    }

    public void delete(Long id) {
        Fee fee = getFee(id);
        feeRepository.delete(fee);
    }

    private Fee getFee(Long id) {
        return feeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fee", id));
    }

    private FeeResponse toResponse(Fee fee) {
        return new FeeResponse(
            fee.getId(),
            studentService.toSummary(fee.getStudent()),
            fee.getAmountDue(),
            fee.getAmountPaid(),
            fee.getDueDate(),
            fee.getPaidDate(),
            fee.getStatus(),
            fee.getPaymentReference(),
            fee.getRemarks()
        );
    }

    private void applyRequest(Fee fee, FeeRequest request) {
        fee.setStudent(studentService.getStudent(request.studentId()));
        fee.setAmountDue(request.amountDue());
        fee.setAmountPaid(request.amountPaid() == null ? BigDecimal.ZERO : request.amountPaid());
        fee.setDueDate(request.dueDate());
        fee.setPaidDate(request.paidDate());
        fee.setStatus(request.status() == null ? FeeStatus.PENDING : request.status());
        fee.setPaymentReference(request.paymentReference());
        fee.setRemarks(request.remarks());
    }
}
