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
        return staffRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        return toResponse(getStaff(id));
    }

    public StaffResponse create(StaffRequest request) {
        Staff staff = new Staff();
        applyRequest(staff, request);
        return toResponse(staffRepository.save(staff));
    }

    public StaffResponse update(Long id, StaffRequest request) {
        Staff staff = getStaff(id);
        applyRequest(staff, request);
        return toResponse(staffRepository.save(staff));
    }

    public void delete(Long id) {
        Staff staff = getStaff(id);
        staffRepository.delete(staff);
    }

    Staff getStaff(Long id) {
        return staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
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

