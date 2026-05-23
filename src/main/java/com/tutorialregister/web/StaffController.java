package com.tutorialregister.web;

import com.tutorialregister.dto.StaffRequest;
import com.tutorialregister.dto.StaffResponse;
import com.tutorialregister.service.StaffService;
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
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<StaffResponse> findAll() {
        return staffService.findAll();
    }

    @GetMapping("/{id}")
    public StaffResponse findById(@PathVariable Long id) {
        return staffService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffResponse create(@Valid @RequestBody StaffRequest request) {
        return staffService.create(request);
    }

    @PutMapping("/{id}")
    public StaffResponse update(@PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return staffService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        staffService.delete(id);
    }
}
