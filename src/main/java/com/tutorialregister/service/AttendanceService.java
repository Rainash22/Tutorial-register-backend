package com.tutorialregister.service;

import com.tutorialregister.dto.AttendanceRequest;
import com.tutorialregister.dto.AttendanceResponse;
import com.tutorialregister.dto.BulkAttendanceRequest;
import com.tutorialregister.dto.BulkAttendanceRequest.BulkAttendanceOverride;
import com.tutorialregister.dto.CourseSummaryResponse;
import com.tutorialregister.model.Attendance;
import com.tutorialregister.model.AttendanceStatus;
import com.tutorialregister.model.Course;
import com.tutorialregister.model.Staff;
import com.tutorialregister.model.Student;
import com.tutorialregister.repository.AttendanceRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final StaffService staffService;
    private final CourseService courseService;
    private final UserAccountService userAccountService;

    public AttendanceService(
        AttendanceRepository attendanceRepository,
        StudentService studentService,
        StaffService staffService,
        @Lazy CourseService courseService,  // @Lazy breaks the circular dependency
        UserAccountService userAccountService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.studentService = studentService;
        this.staffService = staffService;
        this.courseService = courseService;
        this.userAccountService = userAccountService;
    }

    public List<AttendanceResponse> findAll() {
        String username = userAccountService.getCurrentUsername();
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        Long instId = currentUser != null && currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;

        if (userAccountService.hasRole("ADMIN")) {
            return instId == null
                ? attendanceRepository.findAll().stream().map(this::toResponse).toList()
                : attendanceRepository.findByCourseInstitutionId(instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STAFF")) {
            return instId == null
                ? attendanceRepository.findByCourseTeacherUserAccountUsername(username).stream().map(this::toResponse).toList()
                : attendanceRepository.findByCourseInstitutionIdAndCourseTeacherUserAccountUsername(instId, username).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STUDENT")) {
            return attendanceRepository.findByStudentUserAccountUsername(username).stream().map(this::toResponse).toList();
        }
        return List.of();
    }

    public AttendanceResponse findById(Long id) {
        Attendance attendance = getAttendance(id);
        checkAttendanceAccess(attendance);
        return toResponse(attendance);
    }

    public AttendanceResponse create(AttendanceRequest request) {
        if (request.courseId() != null) {
            Course course = courseService.getCourse(request.courseId());
            userAccountService.verifyInstitution(course.getInstitution());
            if (!userAccountService.hasRole("ADMIN")) {
                checkCourseAccess(course);
            }
        } else if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Staff can only mark attendance for assigned courses");
        }
        if (request.studentId() != null) {
            Student student = studentService.getStudent(request.studentId());
            userAccountService.verifyInstitution(student.getInstitution());
        }
        Attendance attendance = new Attendance();
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    /**
     * Marks attendance for every student enrolled on a course in a single
     * transaction.  Each student gets {@code request.defaultStatus()} unless
     * they are listed in {@code request.overrides()}, in which case the
     * override status (and remarks) is used instead.
     * <p>
     * Duplicate records (same student + course + date) are skipped rather
     * than causing a hard failure so that re-submitting the same date is safe.
     *
     * @param courseId the course whose enrolled students will be marked
     * @param request  the bulk attendance request
     * @return the list of created {@link AttendanceResponse} records
     */
    public List<AttendanceResponse> createBulkForCourse(Long courseId, BulkAttendanceRequest request) {
        Course course = courseService.getCourse(courseId);
        if (!userAccountService.hasRole("ADMIN")) {
            checkCourseAccess(course);
        }

        Staff markedBy = request.markedById() == null
            ? null
            : staffService.getStaff(request.markedById());

        AttendanceStatus defaultStatus = request.defaultStatus() == null
            ? AttendanceStatus.PRESENT
            : request.defaultStatus();

        // Build a quick lookup map: studentId -> override
        Map<Long, BulkAttendanceOverride> overrideMap = request.overrides() == null
            ? Map.of()
            : request.overrides().stream()
                .collect(Collectors.toMap(BulkAttendanceOverride::studentId, o -> o));

        // Skip any student that already has a record for this course+date
        List<Attendance> records = course.getStudents().stream()
            .filter(student -> !attendanceRepository.existsByStudentIdAndCourseIdAndAttendanceDate(
                student.getId(), courseId, request.attendanceDate()))
            .map(student -> buildRecord(student, course, markedBy, request, defaultStatus, overrideMap))
            .toList();

        return attendanceRepository.saveAll(records).stream()
            .map(this::toResponse)
            .toList();
    }

    private Attendance buildRecord(
        Student student,
        Course course,
        Staff markedBy,
        BulkAttendanceRequest request,
        AttendanceStatus defaultStatus,
        Map<Long, BulkAttendanceOverride> overrideMap
    ) {
        BulkAttendanceOverride override = overrideMap.get(student.getId());

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setMarkedBy(markedBy);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(override != null ? override.status() : defaultStatus);
        attendance.setRemarks(override != null ? override.remarks() : null);
        return attendance;
    }

    public AttendanceResponse update(Long id, AttendanceRequest request) {
        Attendance attendance = getAttendance(id);
        if (attendance.getCourse() != null) {
            userAccountService.verifyInstitution(attendance.getCourse().getInstitution());
        }
        if (!userAccountService.hasRole("ADMIN")) {
            checkAttendanceAccess(attendance);
            if (request.courseId() != null) {
                Course course = courseService.getCourse(request.courseId());
                checkCourseAccess(course);
            }
        }
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    public void delete(Long id) {
        if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Staff members cannot delete attendance records");
        }
        Attendance attendance = getAttendance(id);
        if (attendance.getCourse() != null) {
            userAccountService.verifyInstitution(attendance.getCourse().getInstitution());
        }
        attendanceRepository.delete(attendance);
    }

    private Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
    }

    private void checkAttendanceAccess(Attendance a) {
        if (a.getCourse() != null) {
            userAccountService.verifyInstitution(a.getCourse().getInstitution());
        } else if (a.getStudent() != null) {
            userAccountService.verifyInstitution(a.getStudent().getInstitution());
        }
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            if (a.getCourse() == null || a.getCourse().getTeacher() == null || a.getCourse().getTeacher().getUserAccount() == null || !username.equals(a.getCourse().getTeacher().getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this attendance record");
            }
        } else if (userAccountService.hasRole("STUDENT")) {
            if (a.getStudent() == null || a.getStudent().getUserAccount() == null || !username.equals(a.getStudent().getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this attendance record");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    private void checkCourseAccess(Course course) {
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            if (course.getTeacher() == null || course.getTeacher().getUserAccount() == null || !username.equals(course.getTeacher().getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not assigned to teach this course");
            }
        } else if (userAccountService.hasRole("STUDENT")) {
            boolean isEnrolled = course.getStudents().stream()
                .anyMatch(s -> s.getUserAccount() != null && username.equals(s.getUserAccount().getUsername()));
            if (!isEnrolled) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not enrolled in this course");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        Course course = attendance.getCourse();
        CourseSummaryResponse courseSummary = course == null ? null
            : new CourseSummaryResponse(course.getId(), course.getName(), course.getCode());

        return new AttendanceResponse(
            attendance.getId(),
            studentService.toSummary(attendance.getStudent()),
            courseSummary,
            staffService.toSummary(attendance.getMarkedBy()),
            attendance.getAttendanceDate(),
            attendance.getStatus(),
            attendance.getRemarks()
        );
    }

    private void applyRequest(Attendance attendance, AttendanceRequest request) {
        attendance.setStudent(studentService.getStudent(request.studentId()));
        attendance.setCourse(request.courseId() == null ? null : courseService.getCourse(request.courseId()));
        attendance.setMarkedBy(request.markedById() == null ? null : staffService.getStaff(request.markedById()));
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());
        attendance.setRemarks(request.remarks());
    }
}
