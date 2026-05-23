package com.tutorialregister.service;

import com.tutorialregister.dto.NotificationRequest;
import com.tutorialregister.dto.NotificationResponse;
import com.tutorialregister.model.Notification;
import com.tutorialregister.model.NotificationStatus;
import com.tutorialregister.repository.NotificationRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentService studentService;
    private final StaffService staffService;

    public NotificationService(
        NotificationRepository notificationRepository,
        StudentService studentService,
        StaffService staffService
    ) {
        this.notificationRepository = notificationRepository;
        this.studentService = studentService;
        this.staffService = staffService;
    }

    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public NotificationResponse findById(Long id) {
        return toResponse(getNotification(id));
    }

    public NotificationResponse create(NotificationRequest request) {
        Notification notification = new Notification();
        applyRequest(notification, request);
        return toResponse(notificationRepository.save(notification));
    }

    public NotificationResponse update(Long id, NotificationRequest request) {
        Notification notification = getNotification(id);
        applyRequest(notification, request);
        return toResponse(notificationRepository.save(notification));
    }

    public void delete(Long id) {
        Notification notification = getNotification(id);
        notificationRepository.delete(notification);
    }

    private Notification getNotification(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            studentService.toSummary(notification.getStudent()),
            staffService.toSummary(notification.getStaff()),
            notification.getTitle(),
            notification.getMessage(),
            notification.getChannel(),
            notification.getScheduledAt(),
            notification.getSentAt(),
            notification.getStatus()
        );
    }

    private void applyRequest(Notification notification, NotificationRequest request) {
        notification.setStudent(request.studentId() == null ? null : studentService.getStudent(request.studentId()));
        notification.setStaff(request.staffId() == null ? null : staffService.getStaff(request.staffId()));
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setChannel(request.channel());
        notification.setScheduledAt(request.scheduledAt());
        notification.setSentAt(request.sentAt());
        notification.setStatus(request.status() == null ? NotificationStatus.DRAFT : request.status());
    }
}
