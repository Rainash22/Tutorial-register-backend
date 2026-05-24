package com.tutorialregister.web;

import com.tutorialregister.dto.CourseRequest;
import com.tutorialregister.dto.CourseResponse;
import com.tutorialregister.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** List all courses. */
    @GetMapping
    public List<CourseResponse> findAll() {
        return courseService.findAll();
    }

    /** Get a single course by ID. */
    @GetMapping("/{id}")
    public CourseResponse findById(@PathVariable Long id) {
        return courseService.findById(id);
    }

    /** Get all courses taught by a specific teacher (staff). */
    @GetMapping("/teacher/{teacherId}")
    public List<CourseResponse> findByTeacher(@PathVariable Long teacherId) {
        return courseService.findByTeacher(teacherId);
    }

    /** Get all courses a specific student is enrolled in. */
    @GetMapping("/student/{studentId}")
    public List<CourseResponse> findByStudent(@PathVariable Long studentId) {
        return courseService.findByStudent(studentId);
    }

    /** Create a new course (optionally with schedules). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    /** Update an existing course (replaces schedules). */
    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    /** Delete a course. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    /** Enrol a student in a course. */
    @PostMapping("/{id}/enrol/{studentId}")
    public CourseResponse enrollStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return courseService.enrollStudent(id, studentId);
    }

    /** Unenrol a student from a course. */
    @DeleteMapping("/{id}/enrol/{studentId}")
    public CourseResponse unenrolStudent(@PathVariable Long id, @PathVariable Long studentId) {
        return courseService.unenrolStudent(id, studentId);
    }
}
