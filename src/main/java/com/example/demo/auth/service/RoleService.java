package com.example.demo.auth.service;

import java.util.HashSet;
import java.util.stream.Collectors;

import com.example.demo.ExceptionHandler.*;
import com.example.demo.auth.Mapper.RoleMapper;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.entity.*;
import com.example.demo.auth.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
public interface RoleService {
// Role management
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO);

    public RoleResponseDTO partialUpdateRole(Long id, UpdateRoleRequest roleRequestDTO) ;

    //public RoleResponseDTO deleteRolePermissions(Long id, List<String> permissions) ;

    public List<RoleResponseDTO> getAllRoles() ;

    public RoleResponseDTO getRoleById(Long id);

    ////public void deleteRole(Long id) ;

    //public void removeRoleFromUser(Long userId) ;
    public List<PermissionResponseDTO> getAllPermissions() ;
}
