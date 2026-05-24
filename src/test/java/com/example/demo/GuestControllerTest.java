package com.example.demo;

import com.example.demo.catalog.controller.GuestController;
import com.example.demo.catalog.dto.Guest.GuestRequestDTO;
import com.example.demo.catalog.dto.Guest.GuestResponseDTO;
import com.example.demo.catalog.service.GuestService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestController.class)
@ActiveProfiles("test")
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestService guestService;

        private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private GuestResponseDTO buildGuestResponse(Long id, String name, String email, String phone) {
        GuestResponseDTO dto = new GuestResponseDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhone(phone);
        return dto;
    }

    private GuestRequestDTO buildGuestRequest(String name, String email, String phone) {
        GuestRequestDTO request = new GuestRequestDTO();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        return request;
    }

    // ── Create Guest ────────────────────────────────────────────────────────

    @Test
    void createGuest_withValidData_returns201() throws Exception {
        GuestRequestDTO request = buildGuestRequest("Jane Doe", "jane@example.com", "+1234567890");
        GuestResponseDTO response = buildGuestResponse(1L, "Jane Doe", "jane@example.com", "+1234567890");

        Mockito.when(guestService.createGuest(any(GuestRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.phone").value("+1234567890"));
    }

    @Test
    void createGuest_withBlankName_returns400() throws Exception {
        GuestRequestDTO request = buildGuestRequest("", "jane@example.com", null);

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGuest_withInvalidEmail_returns400() throws Exception {
        GuestRequestDTO request = buildGuestRequest("Jane", "not-an-email", null);

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGuest_withDuplicateEmail_returns400() throws Exception {
        GuestRequestDTO request = buildGuestRequest("Jane Doe", "jane@example.com", null);

        Mockito.when(guestService.createGuest(any(GuestRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("A guest with email jane@example.com already exists."));

        mockMvc.perform(post("/api/v1/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Get Guest ───────────────────────────────────────────────────────────

    @Test
    void getGuest_whenExists_returns200() throws Exception {
        GuestResponseDTO response = buildGuestResponse(1L, "Jane Doe", "jane@example.com", "+1234567890");

        Mockito.when(guestService.getGuest(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/guests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    void getGuest_whenNotFound_returns404() throws Exception {
        Mockito.when(guestService.getGuest(999L))
                .thenThrow(new ResourceNotFoundException("Guest not found with id: 999"));

        mockMvc.perform(get("/api/v1/guests/999"))
                .andExpect(status().isNotFound());
    }

    // ── Get All Guests ──────────────────────────────────────────────────────

    @Test
    void getAllGuests_returns200() throws Exception {
        GuestResponseDTO guest1 = buildGuestResponse(1L, "Jane Doe", "jane@example.com", null);
        GuestResponseDTO guest2 = buildGuestResponse(2L, "John Smith", "john@example.com", "+9876543210");

        Mockito.when(guestService.getAllGuests()).thenReturn(List.of(guest1, guest2));

        mockMvc.perform(get("/api/v1/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("John Smith"));
    }

    @Test
    void getAllGuests_whenEmpty_returns200AndEmptyList() throws Exception {
        Mockito.when(guestService.getAllGuests()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}


