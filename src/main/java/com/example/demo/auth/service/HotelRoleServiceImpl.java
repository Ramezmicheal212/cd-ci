package com.example.demo.auth.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.ExceptionHandler.HotelRoleNotFoundException;
import com.example.demo.ExceptionHandler.PermissionNotFoundException;
import com.example.demo.ExceptionHandler.RoleAlreadyExistsException;
import com.example.demo.auth.Mapper.HotelRoleMapper;
import com.example.demo.auth.UserDTOs.HotelRoleRequestDTO;
import com.example.demo.auth.UserDTOs.HotelRoleResponseDTO;
import com.example.demo.auth.UserDTOs.PermissionResponseDTO;
import com.example.demo.auth.UserDTOs.UpdateHotelRoleRequest;
import com.example.demo.auth.entity.HotelPermission;
import com.example.demo.auth.entity.HotelRole;
import com.example.demo.auth.repository.HotelPermissionRepository;
import com.example.demo.auth.repository.HotelRoleRepository;
import com.example.demo.auth.repository.UserHotelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelRoleServiceImpl implements HotelRoleService {

    private final HotelRoleRepository hotelRoleRepository;
    private final HotelPermissionRepository hotelPermissionRepository;
    private final UserHotelRepository userHotelRepository;

    @Override
    @Transactional
    public HotelRoleResponseDTO createHotelRole(HotelRoleRequestDTO request) {
        if (hotelRoleRepository.existsByName(request.roleName())) {
            throw new RoleAlreadyExistsException("Hotel role already exists: " + request.roleName());
        }

        Set<String> requestedNames = request.permissions() != null
                ? request.permissions()
                : Collections.emptySet();

        Set<HotelPermission> permissions = requestedNames.isEmpty()
                ? Collections.emptySet()
                : hotelPermissionRepository.findByNameIn(requestedNames);

        Set<String> foundNames = permissions.stream()
                .map(HotelPermission::getName)
                .collect(Collectors.toSet());

        List<String> missingPermissions = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .toList();

        if (!missingPermissions.isEmpty()) {
            throw new PermissionNotFoundException("Hotel permissions not found: " + missingPermissions);
        }

        HotelRole role = HotelRole.builder()
                .name(request.roleName())
                .permissions(new HashSet<>(permissions))
                .build();

        try {
            HotelRole savedRole = hotelRoleRepository.save(role);
            return HotelRoleMapper.toHotelRoleResponseDTO(savedRole);
        } catch (DataIntegrityViolationException ex) {
            throw new RoleAlreadyExistsException("Hotel role already exists: " + request.roleName());
        }
    }

    @Override
    @Transactional
    public HotelRoleResponseDTO partialUpdateHotelRole(Long id, UpdateHotelRoleRequest request) {
        HotelRole role = hotelRoleRepository.findById(id)
                .orElseThrow(() -> new HotelRoleNotFoundException(id));

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

        Map<String, HotelPermission> permissionMap = allNames.isEmpty()
                ? new HashMap<>()
                : hotelPermissionRepository.findByNameIn(allNames).stream()
                        .collect(Collectors.toMap(HotelPermission::getName, p -> p));

        List<String> missingPermissions = allNames.stream()
                .filter(name -> !permissionMap.containsKey(name))
                .toList();

        if (!missingPermissions.isEmpty()) {
            throw new PermissionNotFoundException("Hotel permissions not found: " + missingPermissions);
        }

        if (request.addPermissions() != null) {
            request.addPermissions().forEach(name -> role.addPermission(permissionMap.get(name)));
        }

        if (request.removePermissions() != null) {
            request.removePermissions().forEach(name -> role.getPermissions().remove(permissionMap.get(name)));
        }

        return HotelRoleMapper.toHotelRoleResponseDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelRoleResponseDTO> getAllHotelRoles() {
        return hotelRoleRepository.findAll().stream()
                .map(HotelRoleMapper::toHotelRoleResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HotelRoleResponseDTO getHotelRoleById(Long id) {
        HotelRole role = hotelRoleRepository.findById(id)
                .orElseThrow(() -> new HotelRoleNotFoundException(id));
        return HotelRoleMapper.toHotelRoleResponseDTO(role);
    }

    @Override
    @Transactional
    public void deleteHotelRole(Long id) {
        if (!hotelRoleRepository.existsById(id)) {
            throw new HotelRoleNotFoundException(id);
        }

        if (userHotelRepository.existsByHotelRoleId(id)) {
            throw new IllegalArgumentException("Hotel role is assigned to users and cannot be deleted");
        }

        hotelRoleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> getAllHotelPermissions() {
        return hotelPermissionRepository.findAll().stream()
                .map(permission -> new PermissionResponseDTO(permission.getName()))
                .toList();
    }
}
