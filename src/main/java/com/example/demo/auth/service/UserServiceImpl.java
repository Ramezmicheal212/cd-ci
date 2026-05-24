package com.example.demo.auth.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.ExceptionHandler.PasswordMismatchException;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import com.example.demo.ExceptionHandler.RoleNotFoundException;
import com.example.demo.ExceptionHandler.UserAlreadyTakenException;
import com.example.demo.ExceptionHandler.UserNotFoundException;
import com.example.demo.PagedResponse;
import com.example.demo.auth.Mapper.RoleMapper;
import com.example.demo.auth.UserDTOs.AssignRoleRequestDTO;
import com.example.demo.auth.UserDTOs.HotelPermissionDTO;
import com.example.demo.auth.UserDTOs.RemoveRoleRequestDTO;
import com.example.demo.auth.UserDTOs.ResponseDTO;
import com.example.demo.auth.UserDTOs.UpdatePasswordDTO;
import com.example.demo.auth.UserDTOs.UpdateUsernameDTO;
import com.example.demo.auth.UserDTOs.UpdateUsernameResponseDTO;
import com.example.demo.auth.UserDTOs.UserFilter;
import com.example.demo.auth.UserDTOs.UserResponseDTO;
import com.example.demo.auth.entity.EmploymentStatus;
import com.example.demo.auth.entity.HotelPermission;
import com.example.demo.auth.entity.Permission;
import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.entity.UserHotel;
import com.example.demo.auth.repository.RoleRepository;
import com.example.demo.auth.repository.UserHotelRepository;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.auth.specification.UserSpec;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserHotelRepository userhotelRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ResponseDTO getMyUser(Jwt jwt) {
        /*String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return new ResponseDTO(user.getUsername(), user.getRole().getName());*/
        return new ResponseDTO(jwt.getSubject(), "N/A");
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<UserResponseDTO> getAllUsers(Pageable pageable, UserFilter filter) {
        Specification<User> spec = Specification.where(UserSpec.startsWith(filter.username()))
                .and(UserSpec.hasRole(filter.roleName()))
                .and(UserSpec.hasHotel(filter.hotelName()))
                .and(UserSpec.hasEmail(filter.email()))
                .and(UserSpec.hasPhoneNumber(filter.phoneNumber()))
                .and(UserSpec.isEnabled(filter.enabled()))
                .and(UserSpec.hasFullName(filter.fullName()));
        Page<User> usersPage = userRepository.findAll(spec, pageable);
        Page<UserResponseDTO> userResponseDTOs = usersPage.map(RoleMapper::toUserResponseDTO);
        return PagedResponse.from(usersPage, userResponseDTOs.getContent());
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return RoleMapper.toUserResponseDTO(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Check if user has ADMIN role
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        if (isAdmin) {
            throw new ResourceNotFoundException("Cannot delete user with ADMIN role");

        }

        // Soft delete: disable user instead of permanent deletion
        user.setEnabled(false);
        userRepository.save(user);
        userhotelRepository.deleteByUserId(id);
    }

    @Transactional
    @Override
    public void assignRoleToUser(Long userId, AssignRoleRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isEnabled()) {
            throw new ResourceNotFoundException("Cannot assign role to disabled user");
        }

        Set<Role> roles = request.roleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName)))
                .collect(Collectors.toSet());

        // Add roles using the helper method
        roles.forEach(user::addRole);

        userRepository.save(user);
    }

    @Transactional
    @Override
    public void removeRoleFromUser(Long userId, RemoveRoleRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isEnabled()) {
            throw new ResourceNotFoundException("Cannot remove role from disabled user");
        }

        Role role = roleRepository.findByName(request.roleName())
                .orElseThrow(() -> new RoleNotFoundException(request.roleName()));
        if (user.getRoles().size() <= 1) {
            throw new ResourceNotFoundException("User must have at least one role");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        Long currentUserId = jwt.getClaim("userId");
        if (userId.equals(currentUserId) && role.getName().equals("ADMIN")) {
            throw new ResourceNotFoundException("You cannot remove your own ADMIN role");
        }
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserPassword(UpdatePasswordDTO dto, Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isEnabled()) {
            throw new ResourceNotFoundException("Cannot update password for disabled user");
        }

        System.out.println("User found: " + user.getUsername()); // Debugging line to confirm user retrieval

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException("Current password is incorrect.");
        }
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new PasswordMismatchException("New password and confirmation do not match.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public UpdateUsernameResponseDTO updateUsername(UpdateUsernameDTO dto, Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isEnabled()) {
            throw new ResourceNotFoundException("Cannot update username for disabled user");
        }

        if (dto.newUsername().equals(user.getUsername())) {
            throw new UserAlreadyTakenException("Your username cannot be the same as the current username.");
        }
        if (userRepository.existsByUsername(dto.newUsername())) {
            throw new UserAlreadyTakenException("Username " + dto.newUsername() + " is already taken.");
        }

        user.setUsername(dto.newUsername());
        userRepository.save(user);
        Set<String> authorities = getUserRolesAndPermissions(user);

        String accessToken = tokenService.generateAccessToken(user.getUsername(), authorities, user.getId());

        return new UpdateUsernameResponseDTO(
                user.getId(),
                user.getUsername(),
                accessToken
        );
    }

    public static Set<String> getUserRolesAndPermissions(User user) {

        Set<String> authorities = user.getRoles().stream()
                .flatMap(role -> {
                    Set<String> auths = new HashSet<>();

                    // Role authority
                    auths.add("ROLE_" + role.getName());

                    // Permissions
                    auths.addAll(
                            role.getPermissions().stream()
                                    .map(Permission::getName)
                                    .collect(Collectors.toSet())
                    );

                    return auths.stream();
                })
                .collect(Collectors.toSet());

        return authorities;
    }

    // get user hotel permissions
    public List<HotelPermissionDTO> getUserHotelPermissions() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        Object claimObj = jwt.getClaim("userId");
        if (claimObj == null) {
            throw new ResourceNotFoundException("JWT claim 'userId' is missing");
        }

        Long userId;
        if (claimObj instanceof Number) {
            userId = ((Number) claimObj).longValue();
        } else if (claimObj instanceof String) {
            try {
                userId = Long.parseLong((String) claimObj);
            } catch (NumberFormatException ex) {
                throw new ResourceNotFoundException("JWT claim 'userId' is not a valid number: " + claimObj);
            }
        } else {
            throw new ResourceNotFoundException("Unsupported 'userId' claim type: " + claimObj.getClass().getName());
        }

        List<UserHotel> userHotels = userhotelRepository.findAllByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No hotel associations found for user with ID: " + userId));

        if (userHotels == null || userHotels.isEmpty()) {
            return List.of();
        }

        List<HotelPermissionDTO> result = new ArrayList<>();

        for (UserHotel userHotel : userHotels) {

            boolean isActive = userHotel.getStatus() == EmploymentStatus.ACTIVE;
            boolean hasHotelRole = userHotel.getHotelRole() != null;
            boolean isHotelEnabled = userHotel.getHotel() != null && Boolean.TRUE.equals(userHotel.getHotel().getIsActive());

            if (isActive && hasHotelRole && isHotelEnabled) {

                Set<String> permissions = userHotel.getHotelRole()
                        .getPermissions()
                        .stream()
                        .map(HotelPermission::getName)
                        .collect(Collectors.toSet());

                result.add(new HotelPermissionDTO(
                        userHotel.getHotel().getId(),
                        userHotel.getHotel().getName(),
                        userHotel.getHotelRole().getName(),
                        permissions
                ));
            }
        }

        return result;
    }
}
