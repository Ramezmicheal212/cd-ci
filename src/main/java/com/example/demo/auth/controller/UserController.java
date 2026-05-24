package com.example.demo.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.*;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.ExceptionHandler.*;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;

import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.auth.service.*;

import jakarta.validation.*;

import org.springframework.security.access.prepost.*;

import com.example.demo.PagedResponse;

@RestController
@AllArgsConstructor
@RequestMapping({"/api/v1/users", "/api/users"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "User management and profile endpoints")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get current user",
            description = "Returns profile data for the currently authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Current user returned",
                content = @Content(schema = @Schema(implementation = ResponseDTO.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<ResponseDTO> getMyUser(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(userService.getMyUser(extractJwt(principal)));
    }

    private Jwt extractJwt(Object principal) {
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN') ")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all users", description = "Returns all registered users. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Users returned",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PagedResponse<UserResponseDTO>> getAllUsers(@ModelAttribute UserFilter userFilter, Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable, userFilter));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user by id", description = "Returns a single user by id. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "User returned",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete user", description = "Deletes a user and all their hotel associations. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Assign role to user",
            description = "Assigns a role to a user. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Role assigned to user successfully"
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User or role not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })

    public ResponseEntity<String> assignRoleToUser(
            @PathVariable Long userId,
            @RequestBody AssignRoleRequestDTO request
    ) {
        userService.assignRoleToUser(userId, request);
        return ResponseEntity.ok("Role assigned to user successfully");
    }

    @PatchMapping("/me/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update user password",
            description = "Allows a user to update their own password. Requires authentication and the user can only update their own password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "New password payload",
                    content = @Content(schema = @Schema(implementation = UpdatePasswordDTO.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Password updated"
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<String> updateUserPassword(
            @RequestBody @Valid UpdatePasswordDTO dto,
            @AuthenticationPrincipal Object principal
    ) {
        userService.updateUserPassword(dto, extractJwt(principal));
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove role from user",
            description = "Removes a role from a user. Requires ADMIN role. Users must have at least one role, and you cannot remove your own ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "204",
                description = "Role removed from user successfully"
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User or role not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<String> removeRoleFromUser(
            @PathVariable Long userId,
            @RequestBody RemoveRoleRequestDTO request
    ) {
        userService.removeRoleFromUser(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/username")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update user username",
            description = "Allows a user to update their own username. Requires authentication and the user can only update their own username.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "New username payload",
                    content = @Content(schema = @Schema(type = "string", example = "newusername"))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Username updated",
                content = @Content(schema = @Schema(implementation = UpdateUsernameResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UpdateUsernameResponseDTO> updateUsername(
            @RequestBody @Valid UpdateUsernameDTO dto,
            @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.ok(userService.updateUsername(dto, extractJwt(principal)));
    }


    /*public List<HotelPermissionDTO> getUserHotelPermissions() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
        throw new ResourceNotFoundException("User not authenticated");
    }

    Long userId = ((Number) jwt.getClaim("userId")).longValue();

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
} */
    @GetMapping("/me/hotel-permissions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get user's hotel permissions",
            description = "Returns a list of hotels the user is associated with, their role in each hotel, and the permissions that role grants. Requires authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotel permissions returned",
                content = @Content(schema = @Schema(implementation = HotelPermissionDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<HotelPermissionDTO>> getUserHotelPermissions() {
        return ResponseEntity.ok(userService.getUserHotelPermissions());
    }

}
