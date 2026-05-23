package com.tutorialregister.service;

import com.tutorialregister.dto.RoleRequest;
import com.tutorialregister.dto.RoleResponse;
import com.tutorialregister.model.Role;
import com.tutorialregister.repository.RoleRepository;
import com.tutorialregister.web.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse findById(Long id) {
        return toResponse(getRole(id));
    }

    public RoleResponse create(RoleRequest request) {
        Role role = new Role();
        applyRequest(role, request);
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getRole(id);
        applyRequest(role, request);
        return toResponse(roleRepository.save(role));
    }

    public void delete(Long id) {
        Role role = getRole(id);
        roleRepository.delete(role);
    }

    Role getRole(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    private void applyRequest(Role role, RoleRequest request) {
        role.setName(request.name());
        role.setDescription(request.description());
    }
}
