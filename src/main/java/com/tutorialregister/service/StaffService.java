package com.tutorialregister.service;

import com.tutorialregister.dto.StaffRequest;
import com.tutorialregister.dto.StaffResponse;
import com.tutorialregister.dto.StaffSummaryResponse;
import com.tutorialregister.model.Staff;
import com.tutorialregister.repository.StaffRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserAccountService userAccountService;
    private final CourseService courseService;

    public StaffService(
        StaffRepository staffRepository,
        UserAccountService userAccountService,
        @Lazy CourseService courseService  // @Lazy breaks the circular dependency
    ) {
        this.staffRepository = staffRepository;
        this.userAccountService = userAccountService;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> findAll() {
        String username = userAccountService.getCurrentUsername();
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        Long instId = currentUser != null && currentUser.getInstitution() != null ? currentUser.getInstitution().getId() : null;

        if (userAccountService.hasRole("ADMIN")) {
            return instId == null
                ? staffRepository.findAll().stream().map(this::toResponse).toList()
                : staffRepository.findByInstitutionId(instId).stream().map(this::toResponse).toList();
        } else if (userAccountService.hasRole("STAFF")) {
            return staffRepository.findByUserAccountUsername(username).stream().map(this::toResponse).toList();
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        checkStaffAccess(staff);
        return toResponse(staff);
    }

    public StaffResponse create(StaffRequest request) {
        enforceAdmin();
        Staff staff = new Staff();
        applyRequest(staff, request);
        com.tutorialregister.model.UserAccount currentUser = userAccountService.getCurrentUser();
        if (currentUser != null) {
            staff.setInstitution(currentUser.getInstitution());
        }
        return toResponse(staffRepository.save(staff));
    }

    public StaffResponse update(Long id, StaffRequest request) {
        enforceAdmin();
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        userAccountService.verifyInstitution(staff.getInstitution());
        applyRequest(staff, request);
        return toResponse(staffRepository.save(staff));
    }

    public void delete(Long id) {
        enforceAdmin();
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        userAccountService.verifyInstitution(staff.getInstitution());
        staffRepository.delete(staff);
    }

    private void enforceAdmin() {
        if (!userAccountService.hasRole("ADMIN")) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Admin role required");
        }
    }

    private void checkStaffAccess(Staff staff) {
        userAccountService.verifyInstitution(staff.getInstitution());
        if (userAccountService.hasRole("ADMIN")) {
            return;
        }
        String username = userAccountService.getCurrentUsername();
        if (userAccountService.hasRole("STAFF")) {
            if (staff.getUserAccount() == null || !username.equals(staff.getUserAccount().getUsername())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to staff record");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
    }

    Staff getStaff(Long id) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        checkStaffAccess(staff);
        return staff;
    }

    StaffSummaryResponse toSummary(Staff staff) {
        if (staff == null) {
            return null;
        }
        return new StaffSummaryResponse(staff.getId(), staff.getFullName(), staff.getDesignation());
    }

    private StaffResponse toResponse(Staff staff) {
        return new StaffResponse(
            staff.getId(),
            staff.getFullName(),
            staff.getEmail(),
            staff.getPhone(),
            staff.getDesignation(),
            staff.getGender(),
            staff.getJoinedDate(),
            userAccountService.toSummary(staff.getUserAccount()),
            staff.getTeachingCourses().stream().map(courseService::toSummary).toList()
        );
    }

    private void applyRequest(Staff staff, StaffRequest request) {
        staff.setFullName(request.fullName());
        staff.setEmail(request.email());
        staff.setPhone(request.phone());
        staff.setDesignation(request.designation());
        staff.setGender(request.gender());
        staff.setJoinedDate(request.joinedDate());
        staff.setUserAccount(request.userAccountId() == null ? null : userAccountService.getUserAccount(request.userAccountId()));
    }
}

