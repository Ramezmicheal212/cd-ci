package com.example.demo.catalog.controller;

import com.example.demo.catalog.dto.Guest.GuestRequestDTO;
import com.example.demo.catalog.dto.Guest.GuestResponseDTO;
import com.example.demo.catalog.service.GuestService;
import com.example.demo.ExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
@Tag(name = "Guests", description = "Guest profile endpoints")
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    @Operation(summary = "Create guest", description = "Creates a new guest profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Guest created",
                content = @Content(schema = @Schema(implementation = GuestResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GuestResponseDTO> createGuest(@Valid @RequestBody GuestRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.createGuest(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get guest by id", description = "Returns a guest profile by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guest returned",
                content = @Content(schema = @Schema(implementation = GuestResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Guest not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<GuestResponseDTO> getGuest(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.getGuest(id));
    }

    @GetMapping
    @Operation(summary = "List guests", description = "Returns all guest profiles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guests returned",
                content = @Content(schema = @Schema(implementation = GuestResponseDTO.class)))
    })
    public ResponseEntity<List<GuestResponseDTO>> getAllGuests() {
        return ResponseEntity.ok(guestService.getAllGuests());
    }


    @GetMapping("/me")
    @Operation(summary = "Get my guests", description = "Returns all guest profiles associated with the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guests returned",
                content = @Content(schema = @Schema(implementation = GuestResponseDTO.class)))
    })
    public ResponseEntity<List<GuestResponseDTO>> getMyGuests() {
        return ResponseEntity.ok(guestService.getMyGuests());
    }
}
