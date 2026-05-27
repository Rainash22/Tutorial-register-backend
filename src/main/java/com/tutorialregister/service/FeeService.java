package com.tutorialregister.service;

import com.tutorialregister.dto.CourseSummaryResponse;
import com.tutorialregister.dto.FeeRequest;
import com.tutorialregister.dto.FeeResponse;
import com.tutorialregister.model.Course;
import com.tutorialregister.model.Fee;
import com.tutorialregister.model.FeeStatus;
import com.tutorialregister.model.Student;
import com.tutorialregister.repository.FeeRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    public FeeService(
        FeeRepository feeRepository,
        StudentService studentService,
        @Lazy CourseService courseService  // @Lazy breaks the circular dependency with CourseService
    ) {
        this.feeRepository = feeRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<FeeResponse> findAll() {
        return feeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeeResponse findById(Long id) {
        return toResponse(getFee(id));
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> findByStudent(Long studentId) {
        return feeRepository.findByStudentId(studentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> findByCourse(Long courseId) {
        return feeRepository.findByCourseId(courseId).stream().map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------ //
    //  Admin CRUD                                                          //
    // ------------------------------------------------------------------ //

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

    /**
     * Sets the same due date on every active (non-CANCELLED) fee record.
     * Useful for setting a global payment deadline for all students in a term.
     *
     * @param dueDate the due date to apply to all active fees
     * @return the number of fee records updated
     */
    public int setDueDateForAll(LocalDate dueDate) {
        List<Fee> activeFees = feeRepository.findAll().stream()
            .filter(f -> f.getStatus() != FeeStatus.CANCELLED)
            .toList();
        activeFees.forEach(f -> f.setDueDate(dueDate));
        feeRepository.saveAll(activeFees);
        return activeFees.size();
    }

    // ------------------------------------------------------------------ //
    //  Package-visible hooks called by CourseService                       //
    // ------------------------------------------------------------------ //

    /**
     * Automatically creates a Fee record when a student is enrolled in a course.
     * If a fee already exists for this student-course pair (re-enrolment),
     * a new Fee record is created to capture the new obligation.
     *
     * @param student the enrolled student
     * @param course  the course being enrolled into
     * @return the newly created Fee
     */
    Fee createForEnrolment(Student student, Course course) {
        Fee fee = new Fee();
        fee.setStudent(student);
        fee.setCourse(course);
        BigDecimal courseFee = course.getCourseFee() != null ? course.getCourseFee() : BigDecimal.ZERO;
        fee.setTotalFee(courseFee);
        fee.setOutstandingAmount(courseFee);
        fee.setAmountPaid(BigDecimal.ZERO);
        fee.setStatus(courseFee.compareTo(BigDecimal.ZERO) == 0 ? FeeStatus.PAID : FeeStatus.PENDING);
        return feeRepository.save(fee);
    }

    /**
     * Marks the most recent active fee for this student-course pair as CANCELLED
     * when a student is unenrolled from a course.
     *
     * @param student the student being unenrolled
     * @param course  the course being unenrolled from
     */
    void cancelForUnenrolment(Student student, Course course) {
        feeRepository.findByStudentIdAndCourseId(student.getId(), course.getId())
            .ifPresent(fee -> {
                fee.setStatus(FeeStatus.CANCELLED);
                feeRepository.save(fee);
            });
    }

    /**
     * Reduces the outstanding balance of a fee by the given payment amount.
     * Outstanding is clamped to zero (no negative credit balances).
     * Status is automatically updated: PAID when outstanding reaches zero, PARTIAL otherwise.
     *
     * @param fee           the parent fee record to update
     * @param paymentAmount the positive amount being paid in this installment
     */
    void reduceOutstanding(Fee fee, BigDecimal paymentAmount) {
        BigDecimal newOutstanding = fee.getOutstandingAmount().subtract(paymentAmount);
        // Clamp to zero — no negative outstanding (credit) balances
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }
        fee.setOutstandingAmount(newOutstanding);
        fee.setAmountPaid(fee.getAmountPaid().add(paymentAmount));
        fee.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) == 0 ? FeeStatus.PAID : FeeStatus.PARTIAL);
        feeRepository.save(fee);
    }

    /**
     * Adds back the payment amount to the outstanding balance (used when a
     * FeeHistory record is deleted / reversed).
     *
     * @param fee           the parent fee record to update
     * @param paymentAmount the amount being reversed
     */
    void restoreOutstanding(Fee fee, BigDecimal paymentAmount) {
        BigDecimal newOutstanding = fee.getOutstandingAmount().add(paymentAmount);
        fee.setOutstandingAmount(newOutstanding);
        BigDecimal newPaid = fee.getAmountPaid().subtract(paymentAmount);
        fee.setAmountPaid(newPaid.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newPaid);
        fee.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) == 0 ? FeeStatus.PAID : FeeStatus.PARTIAL);
        feeRepository.save(fee);
    }

    // ------------------------------------------------------------------ //
    //  Package-visible helper used by FeeHistoryService                    //
    // ------------------------------------------------------------------ //

    Fee getFee(Long id) {
        return feeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fee", id));
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    FeeResponse toResponse(Fee fee) {
        CourseSummaryResponse courseSummary = courseService.toSummary(fee.getCourse());
        return new FeeResponse(
            fee.getId(),
            studentService.toSummary(fee.getStudent()),
            courseSummary,
            fee.getTotalFee(),
            fee.getOutstandingAmount(),
            fee.getAmountPaid(),
            fee.getDueDate(),
            fee.getStatus(),
            fee.getRemarks(),
            fee.getCreatedAt(),
            fee.getUpdatedAt()
        );
    }

    private void applyRequest(Fee fee, FeeRequest request) {
        fee.setStudent(studentService.getStudent(request.studentId()));
        fee.setCourse(courseService.getCourse(request.courseId()));
        if (request.totalFee() != null) {
            fee.setTotalFee(request.totalFee());
            // Only reset outstanding on a brand-new record (amountPaid is zero)
            if (fee.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
                fee.setOutstandingAmount(request.totalFee());
            }
        } else if (fee.getId() == null) {
            // New record, no totalFee supplied — fall back to course fee
            BigDecimal courseFee = fee.getCourse().getCourseFee() != null
                ? fee.getCourse().getCourseFee() : BigDecimal.ZERO;
            fee.setTotalFee(courseFee);
            fee.setOutstandingAmount(courseFee);
        }
        fee.setDueDate(request.dueDate());
        fee.setStatus(request.status() == null ? FeeStatus.PENDING : request.status());
        fee.setRemarks(request.remarks());
    }
}
