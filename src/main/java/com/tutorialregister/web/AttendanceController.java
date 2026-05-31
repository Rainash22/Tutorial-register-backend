package com.tutorialregister.web;

import com.tutorialregister.dto.AttendanceRequest;
import com.tutorialregister.dto.AttendanceResponse;
import com.tutorialregister.dto.BulkAttendanceRequest;
import com.tutorialregister.service.AttendanceService;
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
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<AttendanceResponse> findAll() {
        return attendanceService.findAll();
    }

    @GetMapping("/{id}")
    public AttendanceResponse findById(@PathVariable Long id) {
        return attendanceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse create(@Valid @RequestBody AttendanceRequest request) {
        return attendanceService.create(request);
    }

    /**
     * Bulk-mark attendance for all students enrolled on a course.
     * <p>
     * All enrolled students receive {@code defaultStatus} (defaults to
     * {@code PRESENT} if omitted).  Individual students can be given a
     * different status via the {@code overrides} list.
     *
     * <pre>
     * POST /api/attendance/bulk/course/{courseId}
     * {
     *   "attendanceDate": "2025-06-01",
     *   "markedById": 3,
     *   "defaultStatus": "PRESENT",
     *   "overrides": [
     *     { "studentId": 7, "status": "ABSENT", "remarks": "No reason given" }
     *   ]
     * }
     * </pre>
     */
    @PostMapping("/bulk/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public List<AttendanceResponse> createBulkForCourse(
        @PathVariable Long courseId,
        @Valid @RequestBody BulkAttendanceRequest request
    ) {
        return attendanceService.createBulkForCourse(courseId, request);
    }

    @PutMapping("/{id}")
    public AttendanceResponse update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attendanceService.delete(id);
    }
}
