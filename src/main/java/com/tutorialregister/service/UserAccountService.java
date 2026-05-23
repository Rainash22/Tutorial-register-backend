package com.tutorialregister.service;

import com.tutorialregister.dto.RoleResponse;
import com.tutorialregister.dto.UserAccountRequest;
import com.tutorialregister.dto.UserAccountResponse;
import com.tutorialregister.dto.UserSummaryResponse;
import com.tutorialregister.model.Role;
import com.tutorialregister.model.UserAccount;
import com.tutorialregister.repository.RoleRepository;
import com.tutorialregister.repository.UserAccountRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        RoleService roleService,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccountResponse> findAll() {
        return userAccountRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserAccountResponse findById(Long id) {
        return toResponse(getUserAccount(id));
    }

    public UserAccountResponse create(UserAccountRequest request) {
        UserAccount userAccount = new UserAccount();
        applyRequest(userAccount, request);
        return toResponse(userAccountRepository.save(userAccount));
    }

    public UserAccountResponse update(Long id, UserAccountRequest request) {
        UserAccount userAccount = getUserAccount(id);
        applyRequest(userAccount, request);
        return toResponse(userAccountRepository.save(userAccount));
    }

    public void delete(Long id) {
        UserAccount userAccount = getUserAccount(id);
        userAccountRepository.delete(userAccount);
    }

    UserAccount getUserAccount(Long id) {
        return userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    UserSummaryResponse toSummary(UserAccount userAccount) {
        if (userAccount == null) {
            return null;
        }
        return new UserSummaryResponse(userAccount.getId(), userAccount.getUsername(), userAccount.getEmail());
    }

    private UserAccountResponse toResponse(UserAccount userAccount) {
        List<RoleResponse> roles = userAccount.getRoles().stream()
            .sorted(Comparator.comparing(Role::getName))
            .map(roleService::toResponse)
            .toList();
        return new UserAccountResponse(
            userAccount.getId(),
            userAccount.getUsername(),
            userAccount.getEmail(),
            userAccount.isEnabled(),
            roles
        );
    }

    private void applyRequest(UserAccount userAccount, UserAccountRequest request) {
        userAccount.setUsername(request.username());
        userAccount.setEmail(request.email());
        userAccount.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
        userAccount.setEnabled(request.enabled() == null || request.enabled());
        userAccount.setRoles(resolveRoles(request.roleIds()));
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        if (roleIds == null) {
            return roles;
        }
        for (Long roleId : roleIds) {
            roles.add(roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId)));
        }
        return roles;
    }
}
