package com.tutorialregister.web;

import com.tutorialregister.dto.FeeRequest;
import com.tutorialregister.dto.FeeResponse;
import com.tutorialregister.service.FeeService;
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
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping
    public List<FeeResponse> findAll() {
        return feeService.findAll();
    }

    @GetMapping("/{id}")
    public FeeResponse findById(@PathVariable Long id) {
        return feeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeResponse create(@Valid @RequestBody FeeRequest request) {
        return feeService.create(request);
    }

    @PutMapping("/{id}")
    public FeeResponse update(@PathVariable Long id, @Valid @RequestBody FeeRequest request) {
        return feeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        feeService.delete(id);
    }
}
