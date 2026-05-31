package com.tutorialregister.service;

import com.tutorialregister.dto.RoleResponse;
import com.tutorialregister.dto.UserAccountRequest;
import com.tutorialregister.dto.UserAccountResponse;
import com.tutorialregister.dto.UserSummaryResponse;
import com.tutorialregister.model.Role;
import com.tutorialregister.model.UserAccount;
import com.tutorialregister.repository.RoleRepository;
import com.tutorialregister.repository.UserAccountRepository;
import com.tutorialregister.model.Institution;
import com.tutorialregister.repository.InstitutionRepository;
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
    private final InstitutionRepository institutionRepository;

    public UserAccountService(
        UserAccountRepository userAccountRepository,
        RoleRepository roleRepository,
        RoleService roleService,
        PasswordEncoder passwordEncoder,
        InstitutionRepository institutionRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.institutionRepository = institutionRepository;
    }

    public List<UserAccountResponse> findAll() {
        UserAccount currentUser = getCurrentUser();
        if (currentUser == null) return List.of();
        Long instId = currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;
        if (instId == null) {
            return userAccountRepository.findAll().stream().map(this::toResponse).toList();
        }
        return userAccountRepository.findByInstitutionId(instId).stream().map(this::toResponse).toList();
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
        return new UserSummaryResponse(userAccount.getId(), userAccount.getUsername(), userAccount.getEmail(), userAccount.getGeneratedPassword());
    }

    private UserAccountResponse toResponse(UserAccount userAccount) {
        List<RoleResponse> roles = userAccount.getRoles().stream()
            .sorted(Comparator.comparing(Role::getName))
            .map(roleService::toResponse)
            .toList();
        Long instId = userAccount.getInstitution() != null ? userAccount.getInstitution().getId() : null;
        String instCode = userAccount.getInstitution() != null ? userAccount.getInstitution().getCode() : null;
        return new UserAccountResponse(
            userAccount.getId(),
            userAccount.getUsername(),
            userAccount.getEmail(),
            userAccount.isEnabled(),
            roles,
            userAccount.getGeneratedPassword(),
            instId,
            instCode
        );
    }

    private void applyRequest(UserAccount userAccount, UserAccountRequest request) {
        userAccount.setUsername(request.username());
        userAccount.setEmail(request.email());
        userAccount.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
        userAccount.setEnabled(request.enabled() == null || request.enabled());
        userAccount.setRoles(resolveRoles(request.roleIds()));

        Institution institution = null;
        if (request.institutionCode() != null && !request.institutionCode().isBlank()) {
            institution = institutionRepository.findByCodeIgnoreCase(request.institutionCode())
                .orElseGet(() -> {
                    Institution inst = new Institution();
                    inst.setName(request.institutionName() != null && !request.institutionName().isBlank()
                        ? request.institutionName()
                        : request.institutionCode() + " Institution");
                    inst.setCode(request.institutionCode().trim().toUpperCase());
                    return institutionRepository.save(inst);
                });
        } else {
            UserAccount currentUser = getCurrentUser();
            if (currentUser != null) {
                institution = currentUser.getInstitution();
            }
        }
        userAccount.setInstitution(institution);
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

    public String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    public boolean hasRole(String role) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public UserAccountResponse generateCredentialsForStudent(Long studentId, com.tutorialregister.repository.StudentRepository studentRepository) {
        com.tutorialregister.model.Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        UserAccount userAccount = student.getUserAccount();
        String password = generateRandomPassword();
        String username = student.getAdmissionNumber() != null 
            ? student.getAdmissionNumber().toLowerCase().replaceAll("[^a-zA-Z0-9]", "") 
            : "std_" + student.getId();

        if (userAccount == null) {
            userAccount = new UserAccount();
            userAccount.setUsername(username);
            userAccount.setEmail(student.getEmail() != null && !student.getEmail().isBlank() ? student.getEmail() : username + "@tutorial.com");
            userAccount.setRoles(Set.of(roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalArgumentException("STUDENT role not found"))));
        }

        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setGeneratedPassword(password);
        userAccount = userAccountRepository.save(userAccount);

        student.setUserAccount(userAccount);
        studentRepository.save(student);

        return toResponse(userAccount);
    }

    public UserAccountResponse generateCredentialsForStaff(Long staffId, com.tutorialregister.repository.StaffRepository staffRepository) {
        com.tutorialregister.model.Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));

        UserAccount userAccount = staff.getUserAccount();
        String password = generateRandomPassword();
        String staffNameClean = staff.getFullName() != null 
            ? staff.getFullName().toLowerCase().replaceAll("[^a-z0-9]", "") 
            : "staff" + staff.getId();
        String phoneDigits = staff.getPhone() != null ? staff.getPhone().replaceAll("[^0-9]", "") : "";
        String lastTwoDigits = "00";
        if (phoneDigits.length() >= 2) {
            lastTwoDigits = phoneDigits.substring(phoneDigits.length() - 2);
        } else if (!phoneDigits.isEmpty()) {
            lastTwoDigits = "0" + phoneDigits;
        }
        String username = staffNameClean + lastTwoDigits;

        if (userAccount == null) {
            userAccount = new UserAccount();
            userAccount.setUsername(username);
            userAccount.setEmail(staff.getEmail() != null && !staff.getEmail().isBlank() ? staff.getEmail() : username + "@tutorial.com");
            userAccount.setRoles(Set.of(roleRepository.findByName("STAFF")
                .orElseThrow(() -> new IllegalArgumentException("STAFF role not found"))));
        } else {
            userAccount.setUsername(username);
        }

        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setGeneratedPassword(password);
        userAccount = userAccountRepository.save(userAccount);

        staff.setUserAccount(userAccount);
        staffRepository.save(staff);

        return toResponse(userAccount);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append("Pw@");
        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public UserAccount getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userAccountRepository.findByUsername(username).orElse(null);
    }

    public void verifyInstitution(Institution resourceInstitution) {
        UserAccount currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        if (resourceInstitution == null) {
            return;
        }
        if (currentUser.getInstitution() == null || 
            !currentUser.getInstitution().getId().equals(resourceInstitution.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: resource belongs to a different institution");
        }
    }
}
