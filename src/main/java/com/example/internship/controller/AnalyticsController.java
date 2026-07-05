package com.example.internship.controller;

import com.example.internship.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Member 9 + Member 10 — AnalyticsController
 * REST endpoints for system analytics. All endpoints are cached
 * (see AnalyticsService @Cacheable) and require ADMIN role (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "System analytics and reporting endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Issue 8 fix: jobId parameter added so pass-rate can be filtered per job posting.
     * If jobId is omitted, returns the global pass rate across all sessions.
     */
    @Operation(
        summary = "Get interview pass rate",
        description = "Returns pass rate as a percentage. Optionally filter by jobId. Cached for 10 minutes.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Pass rate returned successfully")
        }
    )
    @GetMapping("/pass-rate")
    public Map<String, Object> getPassRate(
            @Parameter(description = "Optional job posting ID to filter by")
            @RequestParam(required = false) Long jobId) {

        double rate = (jobId != null)
            ? analyticsService.getPassRateByJob(jobId)
            : analyticsService.getPassRate();

        return Map.of(
            "passRate", rate,
            "unit", "%",
            "jobId", jobId != null ? jobId : "all"
        );
    }

    @Operation(summary = "Get top skills in demand", description = "Returns most-requested skills across all job postings. Cached.")
    @GetMapping("/top-skills")
    public Map<String, Integer> getTopSkills(
            @Parameter(description = "Number of top skills to return (default 10)")
            @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getTopSkillsInDemand(limit);
    }

    @Operation(summary = "Get application trends by month", description = "Returns monthly application volume for past N months. Cached.")
    @GetMapping("/trends")
    public Map<String, Integer> getTrends(
            @Parameter(description = "Number of past months to include (default 6)")
            @RequestParam(defaultValue = "6") int months) {
        return analyticsService.getApplicationTrendByMonth(months);
    }

    @Operation(summary = "Dashboard summary", description = "All key system metrics in one response.")
    @GetMapping("/dashboard-summary")
    public Map<String, Object> getDashboardSummary() {
        return analyticsService.getSystemMetrics();
    }
}
