package com.tutorialregister.service;

import com.tutorialregister.dto.FeeHistoryRequest;
import com.tutorialregister.dto.FeeHistoryResponse;
import com.tutorialregister.model.Fee;
import com.tutorialregister.model.FeeHistory;
import com.tutorialregister.model.FeeHistoryStatus;
import com.tutorialregister.repository.FeeHistoryRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeeHistoryService {

    private final FeeHistoryRepository feeHistoryRepository;
    private final FeeService feeService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final UserAccountService userAccountService;

    public FeeHistoryService(
        FeeHistoryRepository feeHistoryRepository,
        FeeService feeService,
        StudentService studentService,
        CourseService courseService,
        UserAccountService userAccountService
    ) {
        this.feeHistoryRepository = feeHistoryRepository;
        this.feeService = feeService;
        this.studentService = studentService;
        this.courseService = courseService;
        this.userAccountService = userAccountService;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<FeeHistoryResponse> findAll() {
        return feeHistoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeeHistoryResponse findById(Long id) {
        return toResponse(getHistory(id));
    }

    /** All installments for a specific fee record. */
    @Transactional(readOnly = true)
    public List<FeeHistoryResponse> findByFee(Long feeId) {
        return feeHistoryRepository.findByFeeId(feeId).stream().map(this::toResponse).toList();
    }

    /** Full payment audit trail for a student across all courses. */
    @Transactional(readOnly = true)
    public List<FeeHistoryResponse> findByStudent(Long studentId) {
        return feeHistoryRepository.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------ //
    //  Commands                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Records a payment installment and reduces the parent fee's outstanding balance.
     *
     * @param request installment details
     * @return saved history entry
     */
    public FeeHistoryResponse create(FeeHistoryRequest request) {
        Fee fee = feeService.getFee(request.feeId());
        if (!userAccountService.hasRole("ADMIN")) {
            if (userAccountService.hasRole("STAFF")) {
                String username = userAccountService.getCurrentUsername();
                if (fee.getCourse() == null || fee.getCourse().getTeacher() == null || fee.getCourse().getTeacher().getUserAccount() == null || !username.equals(fee.getCourse().getTeacher().getUserAccount().getUsername())) {
                    throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only record payments for your own students");
                }
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: Students cannot record payments");
            }
        }

        FeeHistory history = new FeeHistory();
        history.setFee(fee);
        history.setStudent(fee.getStudent());
        history.setCourse(fee.getCourse());
        history.setAmountPaid(request.amountPaid());
        history.setPaidDate(request.paidDate());
        history.setPaymentReference(request.paymentReference());
        history.setHistoryStatus(request.historyStatus() == null
            ? FeeHistoryStatus.PAYMENT : request.historyStatus());
        history.setRemarks(request.remarks());

        FeeHistory saved = feeHistoryRepository.save(history);

        // Reduce outstanding balance on the parent fee
        feeService.reduceOutstanding(fee, request.amountPaid());

        return toResponse(saved);
    }

    /**
     * Deletes (reverses) a payment installment and restores the parent fee's outstanding balance.
     *
     * @param id the FeeHistory record to remove
     */
    public void delete(Long id) {
        if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Staff members cannot delete payment history");
        }
        FeeHistory history = getHistory(id);
        Fee fee = history.getFee();
        feeHistoryRepository.delete(history);
        // Restore outstanding balance — payment reversal
        feeService.restoreOutstanding(fee, history.getAmountPaid());
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    private FeeHistory getHistory(Long id) {
        return feeHistoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("FeeHistory", id));
    }

    private FeeHistoryResponse toResponse(FeeHistory h) {
        return new FeeHistoryResponse(
            h.getId(),
            h.getFee().getId(),
            studentService.toSummary(h.getStudent()),
            courseService.toSummary(h.getCourse()),
            h.getAmountPaid(),
            h.getPaidDate(),
            h.getPaymentReference(),
            h.getHistoryStatus(),
            h.getRemarks(),
            h.getCreatedAt()
        );
    }
}
