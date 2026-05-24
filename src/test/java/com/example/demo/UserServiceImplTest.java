package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.demo.ExceptionHandler.*;
import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.entity.*;
import com.example.demo.auth.repository.*;
import com.example.demo.auth.service.UserServiceImpl;
import com.example.demo.auth.service.JwtTokenService;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

   /*@Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserHotelRepository userhotelRepository;
    @Mock private JwtTokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    

    @Test
    void shouldReturnUserDTO_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("tony");
        user.setRoles(new HashSet<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUserById(1L);

        assertThat(result.username()).isEqualTo("tony");
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    

    @Test
    void shouldDeleteUser_whenExists() {
        User user = new User();
        user.setId(1L);
        user.setEnabled(true);
        user.setRoles(new HashSet<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(userhotelRepository).deleteByUserId(1L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrow_whenDeletingNonExistingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(99L));

        verify(userRepository, never()).deleteById(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldAssignRolesToUser() {
        User user = new User();
        user.setRoles(new HashSet<>());

        Role role = new Role();
        role.setName("ADMIN");

        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(Set.of("ADMIN"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        userService.assignRoleToUser(1L, dto);

        assertThat(user.getRoles()).contains(role);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrow_whenRoleNotFound() {
        User user = new User();
        user.setRoles(new HashSet<>());

        AssignRoleRequestDTO dto = new AssignRoleRequestDTO(Set.of("ADMIN"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class,
                () -> userService.assignRoleToUser(1L, dto));
    }

   

    @Test
    void shouldRemoveRoleSuccessfully() {
        Role role = new Role();
        role.setName("USER");

        Role role2 = new Role();
        role2.setName("ADMIN");

        User user = new User();
        user.setRoles(new HashSet<>(Set.of(role, role2)));

        RemoveRoleRequestDTO dto = new RemoveRoleRequestDTO("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        // mock security
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(2L);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContextHolder.getContext().setAuthentication(auth);

        userService.removeRoleFromUser(1L, dto);

        assertThat(user.getRoles()).doesNotContain(role);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrow_whenRemovingLastRole() {
        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setRoles(new HashSet<>(Set.of(role)));

        RemoveRoleRequestDTO dto = new RemoveRoleRequestDTO("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        assertThrows(ResourceNotFoundException.class,
                () -> userService.removeRoleFromUser(1L, dto));
    }

   

    @Test
    void shouldUpdatePasswordSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setPassword("encodedOld");

        UpdatePasswordDTO dto = new UpdatePasswordDTO("old", "new", "new");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encodedNew");

        userService.updateUserPassword(dto, jwt);

        assertThat(user.getPassword()).isEqualTo("encodedNew");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrow_whenWrongPassword() {
        User user = new User();
        user.setPassword("encodedOld");

        UpdatePasswordDTO dto = new UpdatePasswordDTO("wrong", "new", "new");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedOld")).thenReturn(false);

        assertThrows(PasswordMismatchException.class,
                () -> userService.updateUserPassword(dto, jwt));
    }

    @Test
    void shouldThrow_whenPasswordsDontMatch() {
        User user = new User();
        user.setPassword("encodedOld");

        UpdatePasswordDTO dto = new UpdatePasswordDTO("old", "new", "wrong");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);

        assertThrows(PasswordMismatchException.class,
                () -> userService.updateUserPassword(dto, jwt));
    }

    

    @Test
    void shouldUpdateUsernameSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setUsername("old");
        user.setRoles(new HashSet<>());

        UpdateUsernameDTO dto = new UpdateUsernameDTO("new");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(tokenService.generateAccessToken(any(), any(), any()))
                .thenReturn("token123");

        UpdateUsernameResponseDTO result =
                userService.updateUsername(dto, jwt);

        assertThat(result.username()).isEqualTo("new");
        assertThat(result.accessToken()).isEqualTo("token123");
    }

    @Test
    void shouldThrow_whenUsernameSame() {
        User user = new User();
        user.setUsername("same");

        UpdateUsernameDTO dto = new UpdateUsernameDTO("same");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyTakenException.class,
                () -> userService.updateUsername(dto, jwt));
    }

    @Test
    void shouldThrow_whenUsernameExists() {
        User user = new User();
        user.setUsername("old");

        UpdateUsernameDTO dto = new UpdateUsernameDTO("new");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new")).thenReturn(true);

        assertThrows(UserAlreadyTakenException.class,
                () -> userService.updateUsername(dto, jwt));
    }

   

    @Test
    void shouldReturnAuthorities() {
        Role role = new Role();
        role.setName("ADMIN");

        Permission p = new Permission();
        p.setName("READ");

        role.setPermissions(Set.of(p));

        User user = new User();
        user.setRoles(Set.of(role));

        Set<String> result =
                UserServiceImpl.getUserRolesAndPermissions(user);

        assertThat(result).contains("ROLE_ADMIN", "READ");
    }

    @Test
    void shouldReturnEmpty_whenNoRoles() {
        User user = new User();
        user.setRoles(new HashSet<>());

        Set<String> result =
                UserServiceImpl.getUserRolesAndPermissions(user);

        assertThat(result).isEmpty();
    }*/
}