package com.tutorialregister.service;

import com.tutorialregister.dto.ClassScheduleRequest;
import com.tutorialregister.dto.ClassScheduleResponse;
import com.tutorialregister.dto.CourseRequest;
import com.tutorialregister.dto.CourseResponse;
import com.tutorialregister.dto.CourseSummaryResponse;
import com.tutorialregister.model.ClassSchedule;
import com.tutorialregister.model.Course;
import com.tutorialregister.model.Student;
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

    public CourseService(
        CourseRepository courseRepository,
        StaffService staffService,
        @Lazy StudentService studentService  // @Lazy breaks the circular dependency
    ) {
        this.courseRepository = courseRepository;
        this.staffService = staffService;
        this.studentService = studentService;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return toResponse(getCourse(id));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findByStudent(Long studentId) {
        return courseRepository.findByStudentsId(studentId).stream().map(this::toResponse).toList();
    }

    // ------------------------------------------------------------------ //
    //  Commands                                                            //
    // ------------------------------------------------------------------ //

    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getCourse(id);
        applyRequest(course, request);
        return toResponse(courseRepository.save(course));
    }

    public void delete(Long id) {
        Course course = getCourse(id);
        courseRepository.delete(course);
    }

    public CourseResponse enrollStudent(Long courseId, Long studentId) {
        Course course = getCourse(courseId);
        Student student = studentService.getStudent(studentId);
        course.getStudents().add(student);
        return toResponse(courseRepository.save(course));
    }

    public CourseResponse unenrolStudent(Long courseId, Long studentId) {
        Course course = getCourse(courseId);
        Student student = studentService.getStudent(studentId);
        course.getStudents().remove(student);
        return toResponse(courseRepository.save(course));
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
