package com.example.demo.catalog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import com.example.demo.ExceptionHandler.ApiError;
import com.example.demo.catalog.service.AmenityService;
import com.example.demo.catalog.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/amenities")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Amenities", description = "Amenity catalog management endpoints")
@AllArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping("")
    @Operation(summary = "List amenities", description = "Returns all amenities.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Amenities returned",
                content = @Content(schema = @Schema(implementation = AmenityResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<AmenityResponseDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get amenity by id", description = "Returns a single amenity by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Amenity returned",
                content = @Content(schema = @Schema(implementation = AmenityResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Amenity not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AmenityResponseDTO> getAmenityById(@PathVariable Long id) {
        AmenityResponseDTO amenity = amenityService.getAmenityById(id);
        return ResponseEntity.ok(amenity);
    }

    @PostMapping("")
    @Operation(summary = "Create amenity", description = "Creates a new amenity.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Amenity created",
                content = @Content(schema = @Schema(implementation = AmenityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AmenityResponseDTO> createAmenity(@Valid @RequestBody AmenityRequestDTO dto) {
        AmenityResponseDTO createdAmenity = amenityService.createAmenity(dto);
        return new ResponseEntity<>(createdAmenity, HttpStatus.CREATED);
    }

}
