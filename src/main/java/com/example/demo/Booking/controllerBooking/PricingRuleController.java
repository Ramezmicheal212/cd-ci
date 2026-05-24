package com.example.demo.Booking.controllerBooking;

import org.springframework.web.bind.annotation.*;
import com.example.demo.Booking.dtoBooking.Booking.*;
import com.example.demo.Booking.servicesBooking.PricingRuleService;
import com.example.demo.Booking.dtoBooking.Booking.PricingRuleResponseDto;
import com.example.demo.ExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/pricing-rules",  "/api/pricing-rules"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pricing Rules", description = "Pricing rule management endpoints")
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    @GetMapping("")
    @Operation(summary = "List pricing rules", description = "Returns all pricing rules.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rules returned",
                content = @Content(schema = @Schema(implementation = PricingRuleResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<PricingRuleResponseDto>> getAllPricingRules() {
        return ResponseEntity.ok(pricingRuleService.getAllPricingRules());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pricing rule by id", description = "Returns a pricing rule by id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pricing rule returned",
                content = @Content(schema = @Schema(implementation = PricingRuleResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Pricing rule not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PricingRuleResponseDto> getPricingRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(pricingRuleService.getRuleById(id));
    }

    @PostMapping("")
    @Operation(summary = "Create pricing rule", description = "Creates a new pricing rule.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pricing rule created",
                content = @Content(schema = @Schema(implementation = PricingRuleResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<PricingRuleResponseDto> createPricingRule(
            @RequestBody @Valid PricingRuleRequestDto request) {
        PricingRuleResponseDto created = pricingRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
