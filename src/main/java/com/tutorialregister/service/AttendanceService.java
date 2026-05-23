package com.tutorialregister.service;

import com.tutorialregister.dto.AttendanceRequest;
import com.tutorialregister.dto.AttendanceResponse;
import com.tutorialregister.model.Attendance;
import com.tutorialregister.repository.AttendanceRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final StaffService staffService;

    public AttendanceService(
        AttendanceRepository attendanceRepository,
        StudentService studentService,
        StaffService staffService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.studentService = studentService;
        this.staffService = staffService;
    }

    public List<AttendanceResponse> findAll() {
        return attendanceRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AttendanceResponse findById(Long id) {
        return toResponse(getAttendance(id));
    }

    public AttendanceResponse create(AttendanceRequest request) {
        Attendance attendance = new Attendance();
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    public AttendanceResponse update(Long id, AttendanceRequest request) {
        Attendance attendance = getAttendance(id);
        applyRequest(attendance, request);
        return toResponse(attendanceRepository.save(attendance));
    }

    public void delete(Long id) {
        Attendance attendance = getAttendance(id);
        attendanceRepository.delete(attendance);
    }

    private Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            studentService.toSummary(attendance.getStudent()),
            staffService.toSummary(attendance.getMarkedBy()),
            attendance.getAttendanceDate(),
            attendance.getStatus(),
            attendance.getRemarks()
        );
    }

    private void applyRequest(Attendance attendance, AttendanceRequest request) {
        attendance.setStudent(studentService.getStudent(request.studentId()));
        attendance.setMarkedBy(request.markedById() == null ? null : staffService.getStaff(request.markedById()));
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());
        attendance.setRemarks(request.remarks());
    }
}
