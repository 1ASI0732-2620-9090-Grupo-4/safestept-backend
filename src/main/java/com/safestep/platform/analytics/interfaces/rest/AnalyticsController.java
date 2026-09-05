package com.safestep.platform.analytics.interfaces.rest;

import com.safestep.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.safestep.platform.analytics.domain.model.queries.GetCertificatesQuery;
import com.safestep.platform.analytics.domain.model.queries.GetProgressQuery;
import com.safestep.platform.analytics.domain.model.queries.GetSummaryQuery;
import com.safestep.platform.shared.application.services.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics", description = "SafeStep statistics, progress and certificate endpoints")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;
    private final CurrentUserService currentUserService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService, CurrentUserService currentUserService) {
        this.analyticsQueryService = analyticsQueryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/summary/me")
    @Operation(summary = "Get current user analytics summary")
    public ResponseEntity<?> getMySummary() {
        return ResponseEntity.ok(analyticsQueryService.handle(new GetSummaryQuery(currentUserService.username())));
    }

    @GetMapping("/progress/me")
    @Operation(summary = "Get current user progress visuals")
    public ResponseEntity<?> getMyProgress() {
        return ResponseEntity.ok(analyticsQueryService.handle(new GetProgressQuery(currentUserService.username())));
    }

    @GetMapping("/certificates/me")
    @Operation(summary = "Get current user certificates")
    public ResponseEntity<?> getMyCertificates() {
        return ResponseEntity.ok(analyticsQueryService.handle(new GetCertificatesQuery(currentUserService.username())));
    }
}
