package com.enterprise.superadmin.feature_management_service.controller;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureAssignmentRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureAssignmentResponse;
import com.enterprise.superadmin.feature_management_service.services.FeatureAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureAssignmentController {

    private final FeatureAssignmentService
            featureAssignmentService;

    public FeatureAssignmentController(
            FeatureAssignmentService featureAssignmentService) {

        this.featureAssignmentService =
                featureAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<FeatureAssignmentResponse>
    assignFeature(
            @Valid
            @RequestBody
            FeatureAssignmentRequest request,
            Authentication authentication) {

        UUID userId =
                getUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        featureAssignmentService.assignFeature(
                                request,
                                userId
                        )
                );
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<
            List<FeatureAssignmentResponse>>
    getTenantFeatures(
            @PathVariable UUID tenantId) {

        return ResponseEntity.ok(
                featureAssignmentService
                        .getFeaturesByTenant(tenantId)
        );
    }

    private UUID getUserId(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        return UUID.fromString(
                authentication.getName()
        );
    }
}