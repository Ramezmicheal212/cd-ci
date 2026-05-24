package com.example.demo;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.controller.AuthController;
import com.example.demo.auth.service.AuthServiceImpl;
import com.example.demo.ExceptionHandler.IncorrectCredentialsException;
import com.example.demo.ExceptionHandler.UserAlreadyTakenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Set;
import java.util.Map;


import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private LoginResponse buildLoginResponse() {
        return new LoginResponse(
                "access-token-123",
                "refresh-token-456",
                "Bearer",
                900L,
                "testuser",
                Set.of("ROLE_USER", "read:hotels")
        );
    }

    // ── Login Tests ─────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returns200AndLoginResponse() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        LoginResponse response = buildLoginResponse();

        Mockito.when(authService.login("testuser", "password123"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("wronguser", "wrongpass");

        Mockito.when(authService.login("wronguser", "wrongpass"))
                .thenThrow(new IncorrectCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBlankUsername_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Register Tests ──────────────────────────────────────────────────────

    @Test
    void register_withValidData_returns201() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "newuser01",
                "Password123",
                "John Doe",
                "john@example.com",
                "+1234567890"
        );

        Mockito.doNothing().when(authService).register(any(UserRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void register_withDuplicateUsername_returns409() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "existinguser",
                "Password123",
                "John Doe",
                "john@example.com",
                "+1234567890"
        );

        Mockito.doThrow(new UserAlreadyTakenException("existinguser"))
                .when(authService).register(any(UserRequestDTO.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withBlankUsername_returns400() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "",
                "Password123",
                "John Doe",
                "john@example.com",
                "+1234567890"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Refresh Token Tests ─────────────────────────────────────────────────

    @Test
    void refresh_withValidToken_returns200AndLoginResponse() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        LoginResponse response = buildLoginResponse();

        Mockito.when(authService.refreshToken("valid-refresh-token"))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    void refresh_withBlankToken_returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Revoke Token Tests ──────────────────────────────────────────────────

    @Test
    void revoke_withValidToken_returns204() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("token-to-revoke");

        Mockito.doNothing().when(authService).revokeRefreshToken("token-to-revoke");

        mockMvc.perform(post("/api/v1/auth/revoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}

