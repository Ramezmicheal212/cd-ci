package com.example.demo.auth.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.ExceptionHandler.ApiError;
import com.example.demo.auth.UserDTOs.PermissionResponseDTO;
import com.example.demo.auth.UserDTOs.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.auth.service.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/roles",  "/api/roles"})
@AllArgsConstructor
@Tag(name = "Roles", description = "Role and permission management endpoints")
public class RoleController {

    private final RoleServiceImpl roleService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all roles", description = "Returns all available roles. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Roles returned",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get role by id", description = "Returns a single role with its permissions. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Role returned",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create role",
            description = "Creates a role (or updates its permissions if it already exists). Requires ADMIN role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Role data including role name and permission list",
                    content = @Content(schema = @Schema(implementation = RoleRequestDTO.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Role created or updated",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoleResponseDTO> createRole(@RequestBody @Valid RoleRequestDTO roleRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(roleRequestDTO));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update role permissions",
            description = "Adds permissions to an existing role. Requires ADMIN role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Role data including role name and permission list",
                    content = @Content(schema = @Schema(implementation = UpdateRoleRequest.class))
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Role permissions updated",
                content = @Content(schema = @Schema(implementation = RoleResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoleResponseDTO> partialUpdateRole(@PathVariable Long id, @RequestBody @Valid UpdateRoleRequest updateRoleRequest) {
        return ResponseEntity.ok(roleService.partialUpdateRole(id, updateRoleRequest));
    }

    /*@DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete role", description = "Deletes a role by id. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Role not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }*/
    @Operation(
            summary = "Get all permissions",
            description = "Retrieves a list of all available permissions. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "List of permissions retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }

}
