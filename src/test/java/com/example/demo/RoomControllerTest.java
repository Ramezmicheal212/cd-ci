package com.example.demo;

import com.example.demo.catalog.controller.RoomController;
import com.example.demo.catalog.dto.*;
import com.example.demo.catalog.service.RoomServiceImpl;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@ActiveProfiles("test")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean

    private RoomServiceImpl roomService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────
    private RoomResponseDTO buildRoomResponse(Long id, String type, Integer capacity, BigDecimal price, Long hotelId) {
        return new RoomResponseDTO(id, type, capacity, price, Set.of("WiFi"), hotelId, null);
    }

    // ── Create Room ─────────────────────────────────────────────────────────
    /*@Test
    @WithMockUser
    void createRoom_returns201() throws Exception {
        RoomRequestDTO request = new RoomRequestDTO("DELUXE", 2, new BigDecimal("150.00"), Set.<Long>of(1L),  null);
        RoomResponseDTO response = buildRoomResponse(1L, "DELUXE", 2, new BigDecimal("150.00"), 1L);

        Mockito.when(roomService.createRoom(eq(1L), any(RoomRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/hotels/1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomType").value("DELUXE"))
                .andExpect(jsonPath("$.capacity").value(2))
                .andExpect(jsonPath("$.basePrice").value(150.00));
    }*/

    @Test
    @WithMockUser
    void createRoom_withBlankType_returns400() throws Exception {
        RoomRequestDTO request = new RoomRequestDTO("", 2, new BigDecimal("150.00"), Set.<Long>of(), (MultipartFile) null);

        mockMvc.perform(post("/api/v1/hotels/1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createRoom_withZeroCapacity_returns400() throws Exception {
        RoomRequestDTO request = new RoomRequestDTO("SUITE", 0, new BigDecimal("200.00"), Set.<Long>of(), (MultipartFile) null);

        mockMvc.perform(post("/api/v1/hotels/1/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Get All Rooms By Hotel ──────────────────────────────────────────────
    @Test
    @WithMockUser
    void getAllRoomsByHotelId_returns200() throws Exception {
        RoomResponseDTO room = buildRoomResponse(1L, "STANDARD", 2, new BigDecimal("100.00"), 1L);

        Mockito.when(roomService.getAllRoomsByHotelId(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/hotels/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roomType").value("STANDARD"));
    }

    @Test
    @WithMockUser
    void getAllRoomsByHotelId_withFilters_returns200() throws Exception {
        RoomResponseDTO room = buildRoomResponse(1L, "DELUXE", 3, new BigDecimal("200.00"), 1L);

        Mockito.when(roomService.getAllRoomsByHotelId(eq(1L), eq(3), any(), any(), any(), any()))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/hotels/1/rooms")
                .param("capacity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacity").value(3));
    }

    // ── Get Room By Id ──────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void getRoomById_whenExists_returns200() throws Exception {
        RoomResponseDTO response = buildRoomResponse(1L, "SUITE", 4, new BigDecimal("300.00"), 1L);

        Mockito.when(roomService.getRoomById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomType").value("SUITE"));
    }

    @Test
    @WithMockUser
    void getRoomById_whenNotFound_returns404() throws Exception {
        Mockito.when(roomService.getRoomById(999L))
                .thenThrow(new ResourceNotFoundException("Room not found with id: 999"));

        mockMvc.perform(get("/api/v1/rooms/999"))
                .andExpect(status().isNotFound());
    }

    // ── Update Room ─────────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void updateRoom_returns200() throws Exception {
        RoomRequestDTO request = new RoomRequestDTO("UPDATED_SUITE", 4, new BigDecimal("350.00"), Set.<Long>of(), (MultipartFile) null);
        RoomResponseDTO response = buildRoomResponse(1L, "UPDATED_SUITE", 4, new BigDecimal("350.00"), 1L);

        Mockito.when(roomService.updateRoom(eq(1L), eq(1L), any(RoomRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/hotels/1/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("UPDATED_SUITE"))
                .andExpect(jsonPath("$.basePrice").value(350.00));
                
    }

    // ── Patch Room ──────────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void patchRoom_returns200() throws Exception {
        RoomPartialUpdateDTO dto = new RoomPartialUpdateDTO("PREMIUM", null, new BigDecimal("250.00"), null);
        RoomResponseDTO response = buildRoomResponse(1L, "PREMIUM", 3, new BigDecimal("250.00"), 1L);

        Mockito.when(roomService.patchRoom(eq(1L), eq(1L), any(RoomPartialUpdateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/hotels/1/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("PREMIUM"))
                .andExpect(jsonPath("$.basePrice").value(250.00));
    }
}
