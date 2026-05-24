package com.example.demo;

import com.example.demo.PagedResponse;
import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.controller.UserController;
import com.example.demo.auth.service.UserServiceImpl;
import com.example.demo.ExceptionHandler.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

  @MockitoBean

    private UserServiceImpl userService;

                private final ObjectMapper objectMapper = new ObjectMapper()
                                                .registerModule(new JavaTimeModule())
                                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private UserResponseDTO buildUserResponse(Long id, String username, Set<String> roles) {
        return new UserResponseDTO(id, username, roles, List.of());
    }

    private ResponseDTO buildMeResponse(String username) {
        return new ResponseDTO(username, "N/A");
    }

    // ── Get My User ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "testuser")
    void getMyUser_returns200() throws Exception {
        ResponseDTO response = buildMeResponse("testuser");

        Mockito.when(userService.getMyUser(nullable(Jwt.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // ── Get All Users ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returns200() throws Exception {
        PagedResponse<UserResponseDTO> pagedResponse = new PagedResponse<>();
        UserResponseDTO user = buildUserResponse(1L, "testuser", Set.of("USER"));
        pagedResponse.setContent(List.of(user));
        pagedResponse.setPage(0);
        pagedResponse.setSize(20);
        pagedResponse.setTotalElements(1);
        pagedResponse.setTotalPages(1);
        pagedResponse.setLast(true);

        Mockito.when(userService.getAllUsers(any(Pageable.class), any(UserFilter.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ── Get User By Id ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_whenExists_returns200() throws Exception {
        UserResponseDTO response = buildUserResponse(1L, "testuser", Set.of("USER"));

        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_whenNotFound_returns404() throws Exception {
        Mockito.when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    // ── Delete User ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenExists_returns204() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenNotFound_returns404() throws Exception {
        Mockito.doThrow(new UserNotFoundException(999L))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    // ── Assign Role To User ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleToUser_returns200() throws Exception {
        AssignRoleRequestDTO request = new AssignRoleRequestDTO(Set.of("MANAGER"));

        Mockito.doNothing().when(userService)
                .assignRoleToUser(eq(1L), any(AssignRoleRequestDTO.class));

        mockMvc.perform(post("/api/v1/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Role assigned to user successfully"));
    }

    // ── Remove Role From User ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRoleFromUser_returns204() throws Exception {
        RemoveRoleRequestDTO request = new RemoveRoleRequestDTO("MANAGER");

        Mockito.doNothing().when(userService)
                .removeRoleFromUser(eq(1L), any(RemoveRoleRequestDTO.class));

        mockMvc.perform(delete("/api/v1/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    // ── Update Password ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "testuser")
    void updatePassword_returns200() throws Exception {
        UpdatePasswordDTO dto = new UpdatePasswordDTO("oldPass123", "newPass456", "newPass456");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    // ── Update Username ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "testuser")
    void updateUsername_returns200() throws Exception {
        UpdateUsernameDTO dto = new UpdateUsernameDTO("newusername01");
        UpdateUsernameResponseDTO response = new UpdateUsernameResponseDTO(1L, "newusername01", "new-token");

        Mockito.when(userService.updateUsername(any(UpdateUsernameDTO.class), nullable(Jwt.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/me/username")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername01"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUsername_withBlankUsername_returns400() throws Exception {
        UpdateUsernameDTO dto = new UpdateUsernameDTO("");

        mockMvc.perform(patch("/api/v1/users/me/username")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}



