package com.tutorialregister.service;

import com.tutorialregister.dto.ClassScheduleRequest;
import com.tutorialregister.dto.ClassScheduleResponse;
import com.tutorialregister.dto.CourseRequest;
import com.tutorialregister.dto.CourseResponse;
import com.tutorialregister.dto.CourseSummaryResponse;
import com.tutorialregister.model.ClassSchedule;
import com.tutorialregister.model.Course;
import com.tutorialregister.model.Student;
import com.tutorialregister.model.Staff;
import com.tutorialregister.repository.CourseRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final StaffService staffService;
    private final StudentService studentService;
    private final FeeService feeService;
    private final UserAccountService userAccountService;

    public CourseService(
        CourseRepository courseRepository,
        StaffService staffService,
        @Lazy StudentService studentService,  // @Lazy breaks the circular dependency
        @Lazy FeeService feeService,           // @Lazy breaks the circular dependency
        UserAccountService userAccountService
    ) {
        this.courseRepository = courseRepository;
        this.staffService = staffService;
        this.studentService = studentService;
        this.feeService = feeService;
        this.userAccountService = userAccountService;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        String username = userAccountService.getCurrentUsername();
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        Long instId = currentUser != null && currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;

        if (userAccountService.hasRole("ADMIN")) {
            return instId == null
                ? courseRepository.findAll().stream().map(this::toResponse).toList()
                : courseRepository.findByInstitutionId(instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STAFF")) {
            return instId == null
                ? courseRepository.findByTeacherUserAccountUsername(username).stream().map(this::toResponse).toList()
                : courseRepository.findByInstitutionIdAndTeacherUserAccountUsername(instId, username).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STUDENT")) {
            return instId == null
                ? courseRepository.findByStudentsUserAccountUsername(username).stream().map(this::toResponse).toList()
                : courseRepository.findByInstitutionIdAndStudentsUserAccountUsername(instId, username).stream().map(this::toResponse).toList();
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        Course course = getCourse(id);
        checkCourseAccess(course);
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByTeacher(Long teacherId) {
        // Enforce access control: either Admin or the teacher themselves
        if (!userAccountService.hasRole("ADMIN")) {
            String username = userAccountService.getCurrentUsername();
            Staff staff = staffService.getStaff(teacherId);
            if (staff.getUserAccount() == null || !username.equals(staff.getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to teacher's courses");
            }
        }
        return courseRepository.findByTeacherId(teacherId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByStudent(Long studentId) {
        // Enforce access control: Admin, the student themselves, or the staff teaching one of the student's courses
        if (!userAccountService.hasRole("ADMIN")) {
            String username = userAccountService.getCurrentUsername();
            if (userAccountService.hasRole("STUDENT")) {
                Student student = studentService.getStudent(studentId);
                if (student.getUserAccount() == null || !username.equals(student.getUserAccount().getUsername())) {
                    throw new org.springframework.security.access.AccessDeniedException("Access denied to student's courses");
                }
            } else if (userAccountService.hasRole("STAFF")) {
                Student student = studentService.getStudent(studentId);
                boolean isEnrolledInStaffCourse = student.getEnrolledCourses().stream()
                    .anyMatch(c -> c.getTeacher() != null && c.getTeacher().getUserAccount() != null && username.equals(c.getTeacher().getUserAccount().getUsername()));
                if (!isEnrolledInStaffCourse) {
                    throw new org.springframework.security.access.AccessDeniedException("Access denied to student's courses");
                }
            }
        }
        return courseRepository.findByStudentsId(studentId).stream().map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------ //
    //  Commands                                                            //
    // ------------------------------------------------------------------ //

    public CourseResponse create(CourseRequest request) {
        enforceAdmin();
        Course course = new Course();
        applyRequest(course, request);
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        if (currentUser != null) {
            course.setInstitution(currentUser.getInstitution());
        }
        return toResponse(courseRepository.save(course));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        enforceAdmin();
        Course course = getCourse(id);
        userAccountService.verifyInstitution(course.getInstitution());
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    public void delete(Long id) {
        enforceAdmin();
        Course course = getCourse(id);
        userAccountService.verifyInstitution(course.getInstitution());
        courseRepository.delete(course);
    }

    public CourseResponse enrollStudent(Long courseId, Long studentId) {
        enforceAdmin();
        Course course = getCourse(courseId);
        userAccountService.verifyInstitution(course.getInstitution());
        Student student = studentService.getStudent(studentId);
        userAccountService.verifyInstitution(student.getInstitution());
        course.getStudents().add(student);
        CourseResponse response = toResponse(courseRepository.save(course));
        // Auto-create a Fee record for this enrolment
        feeService.createForEnrolment(student, course);
        return response;
    }

    public CourseResponse unenrolStudent(Long courseId, Long studentId) {
        enforceAdmin();
        Course course = getCourse(courseId);
        userAccountService.verifyInstitution(course.getInstitution());
        Student student = studentService.getStudent(studentId);
        userAccountService.verifyInstitution(student.getInstitution());
        course.getStudents().remove(student);
        CourseResponse response = toResponse(courseRepository.save(course));
        // Mark the associated Fee as CANCELLED
        feeService.cancelForUnenrolment(student, course);
        return response;
    }

    private void enforceAdmin() {
        if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Admin role required");
        }
    }

    private void checkCourseAccess(Course course) {
        userAccountService.verifyInstitution(course.getInstitution());
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            if (course.getTeacher() == null || course.getTeacher().getUserAccount() == null || !username.equals(course.getTeacher().getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this course");
            }
        } else if (userAccountService.hasRole("STUDENT")) {
            boolean isEnrolled = course.getStudents().stream()
                .anyMatch(s -> s.getUserAccount() != null && username.equals(s.getUserAccount().getUsername()));
            if (!isEnrolled) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to this course");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    // ------------------------------------------------------------------ //
    //  Package-visible helpers used by other services                      //
    // ------------------------------------------------------------------ //

    Course getCourse(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    CourseSummaryResponse toSummary(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseSummaryResponse(course.getId(), course.getName(), course.getCode());
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
            course.getId(),
            course.getName(),
            course.getCode(),
            course.getDescription(),
            course.getCourseFee(),
            course.getMaxStudents(),
            course.getIsActive(),
            staffService.toSummary(course.getTeacher()),
            course.getSchedules().stream().map(this::toScheduleResponse).toList(),
            course.getStudents().stream().map(studentService::toSummary).toList()
        );
    }

    private ClassScheduleResponse toScheduleResponse(ClassSchedule s) {
        return new ClassScheduleResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(), s.getRoom());
    }

    private void applyRequest(Course course, CourseRequest request) {
        course.setName(request.name());
        course.setCode(request.code());
        course.setDescription(request.description());
        course.setCourseFee(request.courseFee());
        course.setMaxStudents(request.maxStudents());
        course.setIsActive(request.isActive() == null ? Boolean.TRUE : request.isActive());
        course.setTeacher(request.teacherId() == null ? null : staffService.getStaff(request.teacherId()));

        // Replace schedules entirely on each save (orphanRemoval handles old ones)
        course.getSchedules().clear();
        if (request.schedules() != null) {
            for (ClassScheduleRequest sr : request.schedules()) {
                ClassSchedule schedule = new ClassSchedule();
                schedule.setCourse(course);
                schedule.setDayOfWeek(sr.dayOfWeek());
                schedule.setStartTime(sr.startTime());
                schedule.setEndTime(sr.endTime());
                schedule.setRoom(sr.room());
                course.getSchedules().add(schedule);
            }
        }
    }
}
