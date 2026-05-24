package com.example.demo;

import com.example.demo.catalog.controller.AmenityController;
import com.example.demo.catalog.dto.AmenityRequestDTO;
import com.example.demo.catalog.dto.AmenityResponseDTO;
import com.example.demo.catalog.service.AmenityService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AmenityController.class)
@ActiveProfiles("test")
class AmenityControllerTest {

    @Autowired
    private MockMvc mockMvc;

   @MockitoBean

    private AmenityService amenityService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private AmenityResponseDTO buildAmenityResponse(Long id, String name, String description) {
        return new AmenityResponseDTO(id, name, description);
    }

    // ── Get All Amenities ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getAllAmenities_returns200() throws Exception {
        AmenityResponseDTO amenity1 = buildAmenityResponse(1L, "WiFi", "Free wireless internet");
        AmenityResponseDTO amenity2 = buildAmenityResponse(2L, "Pool", "Outdoor swimming pool");

        Mockito.when(amenityService.getAllAmenities()).thenReturn(List.of(amenity1, amenity2));

        mockMvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("WiFi"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Pool"));
    }

    @Test
    @WithMockUser
    void getAllAmenities_whenEmpty_returns200AndEmptyList() throws Exception {
        Mockito.when(amenityService.getAllAmenities()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── Get Amenity By Id ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getAmenityById_whenExists_returns200() throws Exception {
        AmenityResponseDTO response = buildAmenityResponse(1L, "WiFi", "Free wireless internet");

        Mockito.when(amenityService.getAmenityById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/amenities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("Free wireless internet"));
    }

    @Test
    @WithMockUser
    void getAmenityById_whenNotFound_returns404() throws Exception {
        Mockito.when(amenityService.getAmenityById(999L))
                .thenThrow(new ResourceNotFoundException("Amenity not found with id: 999"));

        mockMvc.perform(get("/api/v1/amenities/999"))
                .andExpect(status().isNotFound());
    }

    // ── Create Amenity ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void createAmenity_withValidData_returns201() throws Exception {
        AmenityRequestDTO request = new AmenityRequestDTO("Spa", "Luxury spa services");
        AmenityResponseDTO response = buildAmenityResponse(3L, "Spa", "Luxury spa services");

        Mockito.when(amenityService.createAmenity(any(AmenityRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Spa"))
                .andExpect(jsonPath("$.description").value("Luxury spa services"));
    }

    @Test
    @WithMockUser
    void createAmenity_withBlankName_returns400() throws Exception {
        AmenityRequestDTO request = new AmenityRequestDTO("", "Some description");

        mockMvc.perform(post("/api/v1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

