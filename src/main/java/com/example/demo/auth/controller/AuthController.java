package com.example.demo.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.service.*;
import com.example.demo.ExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@AllArgsConstructor
@RequestMapping({"/api/v1/auth",  "/api/auth"})
@Tag(name = "Authentication", description = "Authentication and RBAC management endpoints")
public class AuthController {

    private final AuthServiceImpl authService;

    // ── Auth ──────────────────────────────────────────────────────────────────
    @PostMapping("/login")             
    @Operation(
            summary = "Login user",    
            description = "Authenticates a user with username and password and returns a JWT access token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login credentials",
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Successful login",
                content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.username(), req.password()));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register user",
            description = "Registers a new user and assigns the default USER role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User registration payload",
                    content = @Content(schema = @Schema(implementation = UserRequestDTO.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = @Content(schema = @Schema(type = "string", example = "User registered successfully"))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Username already taken",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<String> register(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        authService.register(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Logout user",
            description = "Logs out the user by revoking their refresh tokens. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User logged out successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        authService.logout(jwt);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Issues a new access token using a valid refresh token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token payload",
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Token refreshed",
                content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refreshToken(req.refreshToken()));
    }

    @PostMapping("/revoke")
    @Operation(
            summary = "Revoke refresh token",
            description = "Revokes a refresh token so it can no longer be used.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token payload",
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> revoke(@RequestBody @Valid RefreshTokenRequest req) {
        authService.revokeRefreshToken(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    // ── User management (ADMIN) ───────────────────────────────────────────────
    /*@PostMapping("/users/{userId}/hotel-access")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Assign role and hotel access to user",
        description = "Assigns a role (and optionally a hotel) to a user. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role assigned to user successfully",
            content = @Content(schema = @Schema(type = "string", example = "Role and hotel assigned to user successfully"))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User, role or hotel not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<String> assignRoleToUser(
            @PathVariable Long userId,
            @RequestBody @Valid HotelAccessRequest hotelAccessRequest
    ) {
        return ResponseEntity.ok(authService.assignRoleAndHotelToUser(userId, hotelAccessRequest));
    }*/
    // ── Role management (ADMIN) ───────────────────────────────────────────────
}
