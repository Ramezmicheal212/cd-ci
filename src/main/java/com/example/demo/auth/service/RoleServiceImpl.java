package com.example.demo.auth.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.demo.ExceptionHandler.*;
import com.example.demo.auth.Mapper.*;
import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.entity.*;
import com.example.demo.auth.repository.*;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    // Role management
    @Transactional
    @Override
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        if (roleRepository.existsByName(request.roleName())) {
            throw new RoleAlreadyExistsException("Role already exists: " + request.roleName());
        }
        Set<String> requestedNames = request.permissions() != null
                ? request.permissions()
                : Collections.emptySet();

        Set<Permission> permissions = requestedNames.isEmpty()
                ? Collections.emptySet()
                : permissionRepository.findByNameIn(requestedNames);

        Set<String> foundNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<String> missingPermissions = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .toList();

        if (!missingPermissions.isEmpty()) {
            throw new PermissionNotFoundException("Permissions not found: " + missingPermissions);
        }

        Role role = Role.builder()
                .name(request.roleName())
                .permissions(permissions)
                .build();

        try {
            Role savedRole = roleRepository.save(role);
            return RoleMapper.toRoleResponseDTO(savedRole);
        } catch (DataIntegrityViolationException ex) {
            throw new RoleAlreadyExistsException("Role already exists: " + request.roleName());
        }
    }

    @Override
    @Transactional
    public RoleResponseDTO partialUpdateRole(Long id, UpdateRoleRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        if (request.roleName() != null && !request.roleName().isBlank()) {
            role.setName(request.roleName());
        }

        Set<String> allNames = new HashSet<>();
        if (request.addPermissions() != null) {
            allNames.addAll(request.addPermissions());
        }
        if (request.removePermissions() != null) {
            allNames.addAll(request.removePermissions());
        }

        Map<String, Permission> permissionMap = allNames.isEmpty()
                ? new HashMap<>()
                : permissionRepository.findByNameIn(allNames)
                        .stream()
                        .collect(Collectors.toMap(Permission::getName, p -> p));

        List<String> missingPermissions = allNames.stream()
                .filter(name -> !permissionMap.containsKey(name))
                .toList();

        if (!missingPermissions.isEmpty()) {
            throw new PermissionNotFoundException("Permissions not found: " + missingPermissions);
        }

        if (request.addPermissions() != null) {
            request.addPermissions().forEach(name
                    -> role.addPermission(permissionMap.get(name))
            );
        }

        if (request.removePermissions() != null ) {
            request.removePermissions().forEach(name
                    -> role.removePermission(permissionMap.get(name))
            );
        }

        return RoleMapper.toRoleResponseDTO(role);
    }

    /*@Override
    @Transactional
    public RoleResponseDTO deleteRolePermissions(Long id, List<String> permissions) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        Set<Permission> permissionsToRemove = permissions.stream()
                .map(permissionName -> permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + permissionName)))
                .collect(Collectors.toSet());

        permissionsToRemove.forEach(role::removePermission);
        Role updatedRole = roleRepository.save(role);
        return RoleMapper.toRoleResponseDTO(updatedRole);
    }*/

    @Transactional(readOnly = true)
    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleMapper::toRoleResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return RoleMapper.toRoleResponseDTO(role);
    }

    /*@Override
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException(id);
        }
        roleRepository.deleteById(id);
    }*/

    /*@Override
    @Transactional
    public void removeRoleFromUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getRole() != null && !user.getRole().getName().equals("USER")) {
            throw new RuntimeException("Cannot remove role from user with non-USER role");
        }
        // role must be user
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("USER"));
        user.setRole(userRole);

        userRepository.save(user);
    }*/

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permission -> new PermissionResponseDTO(permission.getName()))
                .toList();
    }

}
