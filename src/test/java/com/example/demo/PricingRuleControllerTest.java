package com.example.demo;

import com.example.demo.Booking.controllerBooking.PricingRuleController;
import com.example.demo.Booking.dtoBooking.Booking.*;
import com.example.demo.Booking.servicesBooking.PricingRuleService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PricingRuleController.class)
@ActiveProfiles("test")
class PricingRuleControllerTest {

    /*@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PricingRuleService pricingRuleService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── Helper ──────────────────────────────────────────────────────────────

    private PricingRuleResponseDto buildRuleResponse(Long id, RuleType type, String name, BigDecimal multiplier) {
        return new PricingRuleResponseDto(id, type, name, multiplier);
    }

    // ── Get All Pricing Rules ───────────────────────────────────────────────

    @Test
    @WithMockUser
    void getAllPricingRules_returns200() throws Exception {
        PricingRuleResponseDto rule1 = buildRuleResponse(1L, RuleType.SEASON, "Summer Peak", new BigDecimal("1.5"));
        PricingRuleResponseDto rule2 = buildRuleResponse(2L, RuleType.WEEKEND, "Weekend Rate", new BigDecimal("1.2"));

        Mockito.when(pricingRuleService.getAllPricingRules()).thenReturn(List.of(rule1, rule2));

        mockMvc.perform(get("/api/pricing-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("SEASON"))
                .andExpect(jsonPath("$[0].description").value("Summer Peak"))
                .andExpect(jsonPath("$[0].multiplier").value(1.5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("WEEKEND"))
                .andExpect(jsonPath("$[1].description").value("Weekend Rate"));
    }

    @Test
    @WithMockUser
    void getAllPricingRules_whenEmpty_returns200AndEmptyList() throws Exception {
        Mockito.when(pricingRuleService.getAllPricingRules()).thenReturn(List.of());

        mockMvc.perform(get("/api/pricing-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── Get Pricing Rule By Id ──────────────────────────────────────────────

    @Test
    @WithMockUser
    void getPricingRuleById_whenExists_returns200() throws Exception {
        PricingRuleResponseDto response = buildRuleResponse(1L, RuleType.SEASON, "Summer Peak", new BigDecimal("1.5"));

        Mockito.when(pricingRuleService.getRuleById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/pricing-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("SEASON"))
                .andExpect(jsonPath("$.description").value("Summer Peak"))
                .andExpect(jsonPath("$.multiplier").value(1.5));
    }

    @Test
    @WithMockUser
    void getPricingRuleById_whenNotFound_returns500() throws Exception {
        Mockito.when(pricingRuleService.getRuleById(999L))
                .thenThrow(new RuntimeException("Pricing rule not found"));

        mockMvc.perform(get("/api/pricing-rules/999"))
                .andExpect(status().isInternalServerError());
    }

    // ── Create Pricing Rule ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    void createPricingRule_withSeasonType_returns201() throws Exception {
        PricingRuleRequestDto request = new PricingRuleRequestDto(
                RuleType.SEASON,
                "Winter Discount",
                new BigDecimal("0.8"),
                Set.of("DECEMBER", "JANUARY"),
                null
        );

        PricingRuleResponseDto response = buildRuleResponse(3L, RuleType.SEASON, "Winter Discount", new BigDecimal("0.8"));

        Mockito.when(pricingRuleService.createRule(any(PricingRuleRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pricing-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.type").value("SEASON"))
                .andExpect(jsonPath("$.description").value("Winter Discount"))
                .andExpect(jsonPath("$.multiplier").value(0.8));
    }

    @Test
    @WithMockUser
    void createPricingRule_withWeekendType_returns201() throws Exception {
        PricingRuleRequestDto request = new PricingRuleRequestDto(
                RuleType.WEEKEND,
                "Weekend Surcharge",
                new BigDecimal("1.3"),
                null,
                Set.of(DayOfWeekEnum.SATURDAY, DayOfWeekEnum.SUNDAY)
        );

        PricingRuleResponseDto response = buildRuleResponse(4L, RuleType.WEEKEND, "Weekend Surcharge", new BigDecimal("1.3"));

        Mockito.when(pricingRuleService.createRule(any(PricingRuleRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pricing-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WEEKEND"))
                .andExpect(jsonPath("$.multiplier").value(1.3));
    }

    @Test
    @WithMockUser
    void createPricingRule_withBlankName_returns400() throws Exception {
        PricingRuleRequestDto request = new PricingRuleRequestDto(
                RuleType.SEASON,
                "",
                new BigDecimal("1.5"),
                null,
                null
        );

        mockMvc.perform(post("/api/pricing-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createPricingRule_withNullMultiplier_returns400() throws Exception {
        PricingRuleRequestDto request = new PricingRuleRequestDto(
                RuleType.WEEKEND,
                "Weekend Rate",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/pricing-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }*/
}

