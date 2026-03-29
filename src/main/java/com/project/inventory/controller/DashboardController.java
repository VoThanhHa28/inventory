package com.project.inventory.controller;

import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.dto.dashboard.ActivityItemDto;
import com.project.inventory.dto.dashboard.DashboardStatsDto;
import com.project.inventory.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DashboardController
 * Provides dashboard endpoints for:
 * - KPI statistics (products, low stock, sales)
 * - Recent activity feed
 * 
 * Authorization: All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard statistics and activity endpoints")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * GET /api/dashboard/stats
     * Returns dashboard KPI statistics
     * 
     * @return DashboardStatsDto with aggregated metrics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get dashboard statistics",
        description = "Returns KPI data: total products, low stock count, total sales, and efficiency percentage"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dashboard stats",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardStatsDto.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions (ADMIN role required)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        log.info("GET /api/dashboard/stats");
        
        DashboardStatsDto stats = dashboardService.getStats();
        
        ApiResponse<DashboardStatsDto> response = ApiResponse.<DashboardStatsDto>builder()
            .code(HttpStatus.OK.value())
            .message("Dashboard statistics retrieved successfully")
            .data(stats)
            .build();
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     * GET /api/dashboard/recent-activity
     * Returns recent activity logs across all products
     * 
     * @param limit Maximum number of activities to return (default: 10, max: 100)
     * @return List of recent activities
     */
    @GetMapping("/recent-activity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get recent activity logs",
        description = "Returns recent product changes (create, update, delete) across all products"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved recent activities"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions (ADMIN role required)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<ApiResponse<List<ActivityItemDto>>> getRecentActivity(
        @Parameter(
            description = "Maximum number of activities to return (default: 10, max: 100)",
            example = "10"
        )
        @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit
    ) {
        log.info("GET /api/dashboard/recent-activity?limit={}", limit);
        
        List<ActivityItemDto> activities = dashboardService.getRecentActivity(limit);
        
        ApiResponse<List<ActivityItemDto>> response = ApiResponse.<List<ActivityItemDto>>builder()
            .code(HttpStatus.OK.value())
            .message(String.format("Retrieved %d recent activities", activities.size()))
            .data(activities)
            .build();
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
