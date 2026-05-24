package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.catalog.dto.AmenityRequestDTO;
import com.example.demo.catalog.entity.Amenity;
import com.example.demo.catalog.repository.AmenityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration Test for the Amenity module. Tests the full stack: Controller →
 * Service → Repository → Database (H2).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AmenityIntegrationTest {

    /*@Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AmenityRepository amenityRepository;

    // ── Create Amenity ──────────────────────────────────────────────────────
    @Test
    @Transactional
    void createAmenity_persistsToDatabaseAndReturns201() throws Exception {
        AmenityRequestDTO request = new AmenityRequestDTO("WiFi", "Free wireless internet");

        mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("Free wireless internet"))
                .andExpect(jsonPath("$.id").exists());

        // Verify it was persisted in the database
        assertThat(amenityRepository.findAll()).hasSize(1);
        Amenity saved = amenityRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("WiFi");
        assertThat(saved.getDescription()).isEqualTo("Free wireless internet");
    }

    @Test
    @Transactional
    void createMultipleAmenities_allPersisted() throws Exception {
        AmenityRequestDTO request1 = new AmenityRequestDTO("Pool", "Outdoor swimming pool");
        AmenityRequestDTO request2 = new AmenityRequestDTO("Gym", "Fitness center");
        AmenityRequestDTO request3 = new AmenityRequestDTO("Spa", "Luxury spa services");

        mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        // Verify all three were persisted
        assertThat(amenityRepository.findAll()).hasSize(3);
    }

    // ── Get All Amenities ───────────────────────────────────────────────────
    @Test
    @Transactional
    void getAllAmenities_returnsAllPersistedAmenities() throws Exception {
        // Pre-populate database
        Amenity amenity1 = new Amenity();
        amenity1.setName("WiFi");
        amenity1.setDescription("Free wireless internet");
        amenityRepository.save(amenity1);

        Amenity amenity2 = new Amenity();
        amenity2.setName("Pool");
        amenity2.setDescription("Outdoor swimming pool");
        amenityRepository.save(amenity2);

        mockMvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'WiFi')].description").value("Free wireless internet"))
                .andExpect(jsonPath("$[?(@.name == 'Pool')].description").value("Outdoor swimming pool"));
    }

    @Test
    @Transactional
    void getAllAmenities_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── Get Amenity By Id ───────────────────────────────────────────────────
    @Test
    @Transactional
    void getAmenityById_returnsCorrectAmenity() throws Exception {
        Amenity amenity = new Amenity();
        amenity.setName("Gym");
        amenity.setDescription("24/7 Fitness Center");
        Amenity saved = amenityRepository.save(amenity);

        mockMvc.perform(get("/api/v1/amenities/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Gym"))
                .andExpect(jsonPath("$.description").value("24/7 Fitness Center"));
    }

    @Test
    @Transactional
    void getAmenityById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/amenities/9999"))
                .andExpect(status().isNotFound());
    }

    // ── Full Flow: Create → Retrieve → Verify ──────────────────────────────
    @Test
    @Transactional
    void fullFlow_createAndRetrieveAmenity() throws Exception {
        AmenityRequestDTO request = new AmenityRequestDTO("Parking", "Free underground parking");

        // Step 1: Create
        String responseJson = mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract ID from response
        Long createdId = objectMapper.readTree(responseJson).get("id").asLong();

        // Step 2: Retrieve by ID
        mockMvc.perform(get("/api/v1/amenities/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value("Parking"))
                .andExpect(jsonPath("$.description").value("Free underground parking"));

        // Step 3: Verify in getAll
        mockMvc.perform(get("/api/v1/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Parking"));
    }

    // ── Validation Tests ────────────────────────────────────────────────────
    @Test
    @Transactional
    void createAmenity_withBlankName_returns400AndDoesNotPersist() throws Exception {
        AmenityRequestDTO request = new AmenityRequestDTO("", "Some description");

        mockMvc.perform(post("/api/v1/amenities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(amenityRepository.findAll()).isEmpty();
    }*/
}
