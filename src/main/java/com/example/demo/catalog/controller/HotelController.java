package com.example.demo.catalog.controller;

import java.net.URI;
import java.util.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.PagedResponse;
import com.example.demo.ExceptionHandler.ApiError;
import com.example.demo.catalog.dto.*;
import com.example.demo.auth.UserDTOs.*;
import com.example.demo.catalog.service.HotelServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/hotels")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hotels", description = "Hotel catalog management endpoints")
public class HotelController {

    private final HotelServiceImpl hotelService;

    @GetMapping()
    @Operation(
            summary = "List hotels",
            description = "Returns a paginated list of hotels with optional filters by name, city, and country."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotels retrieved",
                content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<PagedResponse<HotelResponseDTO>> getAllHotels(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country
    ) {
        PagedResponse<HotelResponseDTO> hotels = hotelService.getAllHotels(pageable, name, city, country);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by id", description = "Returns a single hotel by id.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotel returned",
                content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable Long id) {
        HotelResponseDTO hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('SYSTEM_CREATE_HOTEL') or hasRole('ADMIN')") // Only users with this permission or ADMIN role can create hotels
    @Operation(
            summary = "Create hotel",
            description = "Creates a new hotel. Accepts multipart/form-data including optional hotel image files.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = HotelRequestDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Hotel created",
                content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<HotelResponseDTO> createHotel(
            @Valid @ModelAttribute HotelRequestDTO hotelRequestDTO,
            UriComponentsBuilder uriBuilder
    ) throws Exception {
        HotelResponseDTO createdHotel = hotelService.createHotel(hotelRequestDTO);
        URI location = uriBuilder.path("/api/v1/hotels/{id}").buildAndExpand(createdHotel.id()).toUri();
        return ResponseEntity.created(location).body(createdHotel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@hotelSecurity.canUpdateHotel(authentication, #id)") // Only users with permission for this hotel or ADMIN role can update
    @Operation(
            summary = "Update hotel",
            description = "Fully updates a hotel by id using multipart/form-data payload including hotel image files.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = HotelRequestDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotel updated",
                content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<HotelResponseDTO> updateHotel(
            @PathVariable Long id,
            @Valid @ModelAttribute HotelRequestDTO hotelRequestDTO
    ) throws Exception {
        HotelResponseDTO updatedHotel = hotelService.updateHotel(id, hotelRequestDTO);
        return ResponseEntity.ok(updatedHotel);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@hotelSecurity.canUpdateHotel(authentication, #id)")
    @Operation(
            summary = "Partially update hotel",
            description = "Partially updates a hotel with optional multipart/form-data fields and hotel image files.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = HotelPartialUpdateDTO.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotel updated",
                content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid patch payload",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<HotelResponseDTO> partialUpdate(
            @Parameter(description = "Hotel identifier", required = true) @PathVariable Long id,
            @ModelAttribute HotelPartialUpdateDTO partialUpdateDTO
    ) throws Exception {
        HotelResponseDTO updatedHotel = hotelService.partialUpdateHotel(id, partialUpdateDTO);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_DELETE_HOTEL') or @hotelSecurity.canDeleteHotel(authentication, #id)") // Only users with this permission or ADMIN role can delete hotels
    @Operation(summary = "Archive hotel", description = "Archives a hotel by id (soft delete).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Hotel archived"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid archive request",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('SYSTEM_DELETE_HOTEL') or @hotelSecurity.canDeleteHotel(authentication, #id)")
    @Operation(summary = "Archive hotel", description = "Archives a hotel by id (soft delete).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Hotel archived"),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<Void> archiveHotel(@PathVariable Long id) {
        hotelService.archiveHotel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('SYSTEM_UPDATE_HOTEL') or @hotelSecurity.canUpdateHotel(authentication, #id)")
    @Operation(summary = "Restore hotel", description = "Restores an archived hotel and reactivates related records.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Hotel restored"),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<Void> restoreHotel(@PathVariable Long id) {
        hotelService.restoreHotel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/manager")
    @Operation(summary = "Get hotel manager", description = "Returns the manager of a hotel by hotel id.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Hotel manager returned",
                content = @Content(schema = @Schema(implementation = Map.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel or manager not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<Map<String, String>> getHotelManager(@PathVariable Long id) {
        Map<String, String> managerInfo = hotelService.getHotelManager(id);
        return ResponseEntity.ok(managerInfo);
    }

    @PostMapping("/{id}/employees")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.canManageHotelEmployees(authentication, #id)") // Only users with ADMIN role or permission for this hotel can manage employees
    @Operation(summary = "Add employee to hotel", description = "Assigns a user to a hotel with a specific role."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Role and hotel assigned to user successfully",
                content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error or user/role/hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })

    public ResponseEntity<String> addEmployeeToHotel(
            @PathVariable Long id,
            @Valid @RequestBody AssignEmp assignEmpToHotelDTO
    ) {
        System.out.println("RAW BODY = " + assignEmpToHotelDTO);
        hotelService.addEmployeeToHotel(id, assignEmpToHotelDTO);
        return ResponseEntity.ok("Role and hotel assigned to user successfully");
    }

    // Additional endpoints for removing employees, listing employees, etc. can be added similarly with appropriate security checks and documentation.
    @DeleteMapping("/{hotelId}/employees/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.canManageHotelEmployees(authentication, #hotelId)") // Only users with ADMIN role or permission for this hotel can manage employees
    @Operation(summary = "Remove employee from hotel", description = "Removes a user's association with a hotel."
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Employee removed from hotel successfully",
                content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Validation error or user/hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<String> removeEmployeeFromHotel(
            @PathVariable Long hotelId,
            @PathVariable Long userId
    ) {
        hotelService.removeEmployeeFromHotel(hotelId, userId);
        return ResponseEntity.ok("Employee removed from hotel successfully");
    }

    @PatchMapping("/{hotelId}/employees/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.canManageHotelEmployees(authentication, #hotelId)") // Only users with ADMIN role or permission for this hotel can manage employees
        @Operation(summary = "Restore employee access to hotel", description = "Restores an employee's access to a hotel."
        )

        @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee access to hotel restored successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or user/hotel not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
        })
        public ResponseEntity<String> restoreEmployeeAccessToHotel(
                @PathVariable Long hotelId,
                @PathVariable Long userId
        ) {
            hotelService.restoreEmployeeAccessToHotel(hotelId, userId); 
                return ResponseEntity.ok("Employee access to hotel restored successfully");
        }


  

   


    @PatchMapping("/{id}/employees/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.canManageHotelEmployees(authentication, #id)") // Only users with ADMIN role or permission for this hotel can manage employees
        @Operation(summary = "Bulk update hotel employees", description = "Bulk updates employees of a hotel by accepting lists of user IDs to add, remove, or restore."
        )
        @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Hotel employees updated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or user/hotel not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
        })
        public ResponseEntity<String> bulkUpdateHotelEmployees(
                @PathVariable Long id,
                @PathVariable Long userId,


                @Valid @RequestBody BulkUpdateEmployeesRequest bulkUpdateRequest
        ) {
            hotelService.bulkUpdateHotelEmployees(id, userId,  bulkUpdateRequest);
            return ResponseEntity.ok("Hotel employees updated successfully");
        }


      @GetMapping("/{id}/employees")
        @PreAuthorize("hasRole('ADMIN') or @hotelSecurity.canViewHotelEmployees(authentication, #id)") // Only users with ADMIN role or permission for this hotel can view employees
        @Operation(summary = "List hotel employees", description = "Returns a list of employees associated with a hotel."
        )
        @ApiResponses(value = {
        @ApiResponse(   
                responseCode = "200",
                description = "Employees retrieved",
                content = @Content(schema = @Schema(implementation = List.class))
        ),

        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Hotel not found",
           
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
        })

    public ResponseEntity<List<EmployeeResponseDTO>> listHotelEmployees(@PathVariable Long id) {
        List<EmployeeResponseDTO> employees = hotelService.listHotelEmployees(id);
        return ResponseEntity.ok(employees);
    }



}
