package com.example.demo;

import com.example.demo.catalog.controller.HotelController;
import com.example.demo.catalog.dto.*;
import com.example.demo.auth.UserDTOs.HotelRoleRequestDTO;
import com.example.demo.auth.UserDTOs.UserFilter;
import com.example.demo.auth.entity.EmploymentStatus;
import com.example.demo.catalog.service.HotelServiceImpl;
import com.example.demo.catalog.service.HotelSecurity;
import com.example.demo.ExceptionHandler.HotelNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelController.class)
@ActiveProfiles("test")
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelServiceImpl hotelService;

    @MockitoBean
    private HotelSecurity hotelSecurity;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────
    private HotelResponseDTO buildHotelResponse(Long id, String name, String city) {
        return new HotelResponseDTO(id, name, "123 Main St", city, "USA", "A nice hotel", "hotel.jpg", List.of("hotel.jpg"));
    }

    // ── Get All Hotels ──────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void getAllHotels_returns200() throws Exception {
        PagedResponse<HotelResponseDTO> paged = new PagedResponse<>();
        paged.setContent(List.of(buildHotelResponse(1L, "Grand Hotel", "NYC")));
        paged.setPage(0);
        paged.setSize(20);
        paged.setTotalElements(1);
        paged.setTotalPages(1);
        paged.setLast(true);

        Mockito.when(hotelService.getAllHotels(any(Pageable.class), any(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/v1/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Grand Hotel"))
                .andExpect(jsonPath("$.content[0].city").value("NYC"));
    }

    @Test
    @WithMockUser
    void getAllHotels_withFilters_returns200() throws Exception {
        PagedResponse<HotelResponseDTO> paged = new PagedResponse<>();
        paged.setContent(List.of(buildHotelResponse(1L, "Beach Resort", "Miami")));
        paged.setPage(0);
        paged.setSize(20);
        paged.setTotalElements(1);
        paged.setTotalPages(1);
        paged.setLast(true);

        Mockito.when(hotelService.getAllHotels(any(Pageable.class), eq("Beach"), eq("Miami"), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/v1/hotels")
                .param("name", "Beach")
                .param("city", "Miami"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Beach Resort"));
    }

    // ── Get Hotel By Id ─────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void getHotelById_whenExists_returns200() throws Exception {
        HotelResponseDTO response = buildHotelResponse(1L, "Grand Hotel", "NYC");

        Mockito.when(hotelService.getHotelById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Grand Hotel"));
    }

    @Test
    @WithMockUser
    void getHotelById_whenNotFound_returns404() throws Exception {
        Mockito.when(hotelService.getHotelById(999L))
                .thenThrow(new HotelNotFoundException(999L));

        mockMvc.perform(get("/api/v1/hotels/999"))
                .andExpect(status().isNotFound());
    }

    // ── Create Hotel ────────────────────────────────────────────────────────
    @Test
    @WithMockUser(authorities = {"SYSTEM_CREATE_HOTEL"})
    void createHotel_returns201() throws Exception {
        HotelResponseDTO response = buildHotelResponse(1L, "New Hotel", "LA");

        Mockito.when(hotelService.createHotel(any(HotelRequestDTO.class))).thenReturn(response);

        MockMultipartFile hotelImage1 = new MockMultipartFile(
                "hotelImages",
                "front.jpg",
                "image/jpeg",
                "front".getBytes());
        MockMultipartFile hotelImage2 = new MockMultipartFile(
                "hotelImages",
                "lobby.jpg",
                "image/jpeg",
                "lobby".getBytes());

        mockMvc.perform(multipart("/api/v1/hotels")
                .file(hotelImage1)
                .file(hotelImage2)
                .param("name", "New Hotel")
                .param("address", "456 Oak Ave")
                .param("city", "LA")
                .param("country", "USA")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Hotel"));
    }

    // ── Update Hotel ────────────────────────────────────────────────────────
    @Test
    @WithMockUser
    void updateHotel_whenExists_returns200() throws Exception {
        HotelResponseDTO response = buildHotelResponse(1L, "Updated Hotel", "NYC");

        Mockito.when(hotelSecurity.canUpdateHotel(any(), eq(1L))).thenReturn(true);
        Mockito.when(hotelService.updateHotel(eq(1L), any(HotelRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/hotels/1")
                .param("name", "Updated Hotel")
                .param("address", "123 Main St")
                .param("city", "NYC")
                .param("country", "USA")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hotel"));
    }

    // ── Partial Update Hotel ────────────────────────────────────────────────
    @Test
    @WithMockUser
    void partialUpdateHotel_whenExists_returns200() throws Exception {
        HotelResponseDTO response = buildHotelResponse(1L, "Patched Hotel", "NYC");

        Mockito.when(hotelSecurity.canUpdateHotel(any(), eq(1L))).thenReturn(true);
        Mockito.when(hotelService.partialUpdateHotel(eq(1L), any(HotelPartialUpdateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/hotels/1")
                .param("name", "Patched Hotel")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Hotel"));
    }

    // ── Delete Hotel ────────────────────────────────────────────────────────
    @Test
    @WithMockUser(authorities = {"SYSTEM_DELETE_HOTEL"})
    void deleteHotel_whenExists_returns204() throws Exception {
        Mockito.doNothing().when(hotelService).deleteHotel(1L);

        mockMvc.perform(delete("/api/v1/hotels/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"SYSTEM_DELETE_HOTEL"})
    void deleteHotel_whenNotFound_returns404() throws Exception {
        Mockito.doThrow(new HotelNotFoundException(999L))
                .when(hotelService).deleteHotel(999L);

        mockMvc.perform(delete("/api/v1/hotels/999"))
                .andExpect(status().isNotFound());
    }

    // ── Get Hotel Manager ───────────────────────────────────────────────────
    @Test
    @WithMockUser
    void getHotelManager_returns200() throws Exception {
        Mockito.when(hotelService.getHotelManager(1L))
                .thenReturn(Map.of("managerName", "John", "managerEmail", "john@hotel.com"));

        mockMvc.perform(get("/api/v1/hotels/1/manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerName").value("John"));
    }

    // ── Employee Management ─────────────────────────────────────────────────
    @Test
    @WithMockUser(roles = "ADMIN")
    void addEmployeeToHotel_returns200() throws Exception {
        AssignEmp request = new AssignEmp(5L, 2L);

        Mockito.doNothing().when(hotelService).addEmployeeToHotel(eq(1L), any(AssignEmp.class));

        mockMvc.perform(post("/api/v1/hotels/1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeEmployeeFromHotel_returns200() throws Exception {
        Mockito.doNothing().when(hotelService).removeEmployeeFromHotel(1L, 5L);

        mockMvc.perform(delete("/api/v1/hotels/1/employees/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listHotelEmployees_returns200() throws Exception {
        EmployeeResponseDTO emp = new EmployeeResponseDTO(5L, "john", "john@email.com" , "Manager", EmploymentStatus.ACTIVE);

        Mockito.when(hotelService.listHotelEmployees(1L)).thenReturn(List.of(emp));

        mockMvc.perform(get("/api/v1/hotels/1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("john"));
    }
}
