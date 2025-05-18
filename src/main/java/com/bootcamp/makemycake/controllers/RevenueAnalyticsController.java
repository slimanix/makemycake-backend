package com.bootcamp.makemycake.controllers;

import com.bootcamp.makemycake.dto.RevenueAnalyticsResponse;
import com.bootcamp.makemycake.services.RevenueAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
@Tag(name = "Revenue Analytics", description = "APIs for revenue analytics")
@Slf4j
public class RevenueAnalyticsController {

    private final RevenueAnalyticsService revenueAnalyticsService;

    @GetMapping("/patissier/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATISSIER') and (hasRole('ADMIN') or @securityService.isPatisserieOwner(#id))")
    @Operation(
        summary = "Get revenue analytics for a patissier",
        description = "Retrieves revenue data for a specific patissier grouped by time intervals"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved revenue data",
            content = @Content(schema = @Schema(implementation = RevenueAnalyticsResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters provided"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to access this resource"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patisserie not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<?> getPatissierRevenue(
            @Parameter(description = "ID of the patissier") @PathVariable Long id,
            @Parameter(description = "Time interval (day, week, month, year)") 
            @RequestParam(defaultValue = "month") String interval,
            @Parameter(description = "Number of periods to return (1-365)") 
            @RequestParam(defaultValue = "12") int count) {
        
        try {
            log.info("Fetching revenue data for patissier {} with interval {} and count {}", id, interval, count);
            RevenueAnalyticsResponse response = revenueAnalyticsService.getPatissierRevenue(id, interval, count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameters for revenue request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing revenue request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving revenue data: " + e.getMessage());
        }
    }
} 