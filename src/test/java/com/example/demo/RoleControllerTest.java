package com.example.demo;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.controller.RoleController;
import com.example.demo.auth.service.RoleServiceImpl;
import com.example.demo.ExceptionHandler.RoleNotFoundException;
import com.example.demo.ExceptionHandler.RoleAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;


import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@ActiveProfiles("test")
class RoleControllerTest {

    /*/*@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleServiceImpl roleService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private RoleResponseDTO buildRoleResponse(Long id, String name, List<String> permissions) {
        return new RoleResponseDTO(id, name, permissions);
    }

    // ── Get All Roles ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRoles_returns200AndRoleList() throws Exception {
        RoleResponseDTO role = buildRoleResponse(1L, "ADMIN", List.of("read:users", "write:users"));

        Mockito.when(roleService.getAllRoles()).thenReturn(List.of(role));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$[0].permissions[0]").value("read:users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRoles_whenEmpty_returns200AndEmptyList() throws Exception {
        Mockito.when(roleService.getAllRoles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── Get Role By Id ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleById_whenExists_returns200() throws Exception {
        RoleResponseDTO role = buildRoleResponse(1L, "MANAGER", List.of("read:hotels"));

        Mockito.when(roleService.getRoleById(1L)).thenReturn(role);

        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roleName").value("MANAGER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoleById_whenNotFound_returns404() throws Exception {
        Mockito.when(roleService.getRoleById(99L))
                .thenThrow(new RoleNotFoundException(99L));

        mockMvc.perform(get("/api/v1/roles/99"))
                .andExpect(status().isNotFound());
    }

    // ── Create Role ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_withValidData_returns201() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO("RECEPTIONIST", Set.of("read:bookings"));
        RoleResponseDTO response = buildRoleResponse(2L, "RECEPTIONIST", List.of("read:bookings"));

        Mockito.when(roleService.createRole(any(RoleRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("RECEPTIONIST"))
                .andExpect(jsonPath("$.permissions[0]").value("read:bookings"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_withExistingName_returnsConflict() throws Exception {
        RoleRequestDTO request = new RoleRequestDTO("ADMIN", Set.of("read:all"));

        Mockito.when(roleService.createRole(any(RoleRequestDTO.class)))
                .thenThrow(new RoleAlreadyExistsException("Role already exists: ADMIN"));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ── Partial Update Role ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void partialUpdateRole_withValidData_returns200() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(null, List.of("write:bookings"), null);
        RoleResponseDTO response = buildRoleResponse(1L, "MANAGER", List.of("read:hotels", "write:bookings"));

        Mockito.when(roleService.partialUpdateRole(eq(1L), any(UpdateRoleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── Delete Role ─────────────────────────────────────────────────────────

    /*@Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_whenExists_returns204() throws Exception {
        Mockito.doNothing().when(roleService).deleteRole(1L);

        mockMvc.perform(delete("/api/v1/roles/1"))
                .andExpect(status().isNoContent());
    }*/

    /*@Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_whenNotFound_returns404() throws Exception {
        Mockito.doThrow(new RoleNotFoundException(99L))
                .when(roleService).deleteRole(99L);

        mockMvc.perform(delete("/api/v1/roles/99"))
                .andExpect(status().isNotFound());
    }*/

    // ── Get All Permissions ─────────────────────────────────────────────────

    /*@Test
    @WithMockUser(roles = "ADMIN")
    void getAllPermissions_returns200() throws Exception {
        PermissionResponseDTO perm1 = new PermissionResponseDTO("read:users");
        PermissionResponseDTO perm2 = new PermissionResponseDTO("write:users");

        Mockito.when(roleService.getAllPermissions()).thenReturn(List.of(perm1, perm2));

        mockMvc.perform(get("/api/v1/roles/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].permissionName").value("read:users"))
                .andExpect(jsonPath("$[1].permissionName").value("write:users"));
    }*/
}

