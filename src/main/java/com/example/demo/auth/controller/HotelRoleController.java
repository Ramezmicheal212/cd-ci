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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.ExceptionHandler.ApiError;
import com.example.demo.auth.UserDTOs.HotelRoleRequestDTO;
import com.example.demo.auth.UserDTOs.HotelRoleResponseDTO;
import com.example.demo.auth.UserDTOs.PermissionResponseDTO;
import com.example.demo.auth.UserDTOs.UpdateHotelRoleRequest;
import com.example.demo.auth.service.HotelRoleServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping({"/api/v1/hotel-roles",  "/api/hotel-roles"})
@AllArgsConstructor
@Tag(name = "Hotel Roles", description = "Hotel-level role and permission management endpoints")
public class HotelRoleController {

    private final HotelRoleServiceImpl hotelRoleService;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all hotel roles", description = "Returns all hotel-level roles. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel roles returned", content = @Content(schema = @Schema(implementation = HotelRoleResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<HotelRoleResponseDTO>> getAllHotelRoles() {
        return ResponseEntity.ok(hotelRoleService.getAllHotelRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get hotel role by id", description = "Returns a single hotel role with its permissions. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel role returned", content = @Content(schema = @Schema(implementation = HotelRoleResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Hotel role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<HotelRoleResponseDTO> getHotelRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelRoleService.getHotelRoleById(id));
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create hotel role", description = "Creates a new hotel-level role. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Hotel role created", content = @Content(schema = @Schema(implementation = HotelRoleResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<HotelRoleResponseDTO> createHotelRole(@RequestBody @Valid HotelRoleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelRoleService.createHotelRole(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update hotel role", description = "Updates hotel role name and permissions. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel role updated", content = @Content(schema = @Schema(implementation = HotelRoleResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Hotel role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<HotelRoleResponseDTO> partialUpdateHotelRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateHotelRoleRequest request
    ) {
        return ResponseEntity.ok(hotelRoleService.partialUpdateHotelRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete hotel role", description = "Deletes a hotel role if it is not assigned to users. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel role deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Hotel role not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<String> deleteHotelRole(@PathVariable Long id) {
        hotelRoleService.deleteHotelRole(id);
        return ResponseEntity.ok("Hotel role deleted successfully");
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all hotel permissions", description = "Retrieves all available hotel permissions. Requires ADMIN role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel permissions returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<PermissionResponseDTO>> getAllHotelPermissions() {
        return ResponseEntity.ok(hotelRoleService.getAllHotelPermissions());
    }
}
