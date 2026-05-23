package com.tutorialregister.web;

import com.tutorialregister.dto.NotificationRequest;
import com.tutorialregister.dto.NotificationResponse;
import com.tutorialregister.service.NotificationService;
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
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> findAll() {
        return notificationService.findAll();
    }

    @GetMapping("/{id}")
    public NotificationResponse findById(@PathVariable Long id) {
        return notificationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
        return notificationService.create(request);
    }

    @PutMapping("/{id}")
    public NotificationResponse update(@PathVariable Long id, @Valid @RequestBody NotificationRequest request) {
        return notificationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        notificationService.delete(id);
    }
}
