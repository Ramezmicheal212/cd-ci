package com.example.demo.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// modelattribute
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.catalog.dto.RoomPartialUpdateDTO;
import com.example.demo.catalog.dto.RoomRequestDTO;
import com.example.demo.catalog.dto.RoomResponseDTO;
import com.example.demo.catalog.service.RoomServiceImpl;
import com.example.demo.ExceptionHandler.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping({"/api/v1",  "/api"})
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Rooms", description = "Room catalog management endpoints")
public class RoomController {

    private final RoomServiceImpl roomService;

    @PostMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Create room", description = "Creates a room under a hotel.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Room created",
                content = @Content(schema = @Schema(implementation = RoomResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponseDTO> createRoom(@PathVariable Long hotelId,
            @Valid @ModelAttribute RoomRequestDTO requestDTO) throws Exception {
        RoomResponseDTO response = roomService.createRoom(hotelId, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "List hotel rooms", description = "Returns rooms of a hotel with optional filters.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rooms returned",
                content = @Content(schema = @Schema(implementation = RoomResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<RoomResponseDTO>> getAllRoomsByHotelId(@PathVariable Long hotelId, @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Set<String> amenities) {
        return ResponseEntity.ok(roomService.getAllRoomsByHotelId(hotelId, capacity, minPrice, maxPrice, roomType, amenities));
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get room by id", description = "Returns a room by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room returned",
                content = @Content(schema = @Schema(implementation = RoomResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @PutMapping("/hotels/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Update room", description = "Replaces room data for a hotel room.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room updated",
                content = @Content(schema = @Schema(implementation = RoomResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Room or hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable Long hotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequestDTO requestDTO) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, hotelId, requestDTO));
    }

    @PatchMapping("/hotels/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Patch room", description = "Partially updates room fields.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room patched",
                content = @Content(schema = @Schema(implementation = RoomResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RoomResponseDTO> patchRoom(@PathVariable Long hotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomPartialUpdateDTO partialUpdateDTO) {
        return ResponseEntity.ok(roomService.patchRoom(roomId, hotelId, partialUpdateDTO));
    }

    @DeleteMapping("/rooms/{roomId}")
    @PreAuthorize("hasAuthority('SYSTEM_DELETE_ROOM')")
    @Operation(summary = "Archive room", description = "Archives a room (soft delete).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Room archived"),
        @ApiResponse(responseCode = "400", description = "Room has existing bookings",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/rooms/{roomId}/archive")
    @PreAuthorize("hasAuthority('SYSTEM_DELETE_ROOM')")
    @Operation(summary = "Archive room", description = "Archives a room (soft delete).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Room archived"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> archiveRoom(@PathVariable Long roomId) {
        roomService.archiveRoom(roomId);
        return ResponseEntity.noContent().build();
    }

}
