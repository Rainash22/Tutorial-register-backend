package com.tutorialregister.web;

import com.tutorialregister.dto.UserAccountRequest;
import com.tutorialregister.dto.UserAccountResponse;
import com.tutorialregister.service.UserAccountService;
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
@RequestMapping("/api/users")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public List<UserAccountResponse> findAll() {
        return userAccountService.findAll();
    }

    @GetMapping("/{id}")
    public UserAccountResponse findById(@PathVariable Long id) {
        return userAccountService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountResponse create(@Valid @RequestBody UserAccountRequest request) {
        return userAccountService.create(request);
    }

    @PutMapping("/{id}")
    public UserAccountResponse update(@PathVariable Long id, @Valid @RequestBody UserAccountRequest request) {
        return userAccountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userAccountService.delete(id);
    }
}
