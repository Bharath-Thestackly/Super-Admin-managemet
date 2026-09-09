package com.enterprise.superadmin.feature_management_service.controller;


import com.enterprise.superadmin.feature_management_service.dto.request.FeatureCreateRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureResponse;
import com.enterprise.superadmin.feature_management_service.services.FeatureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private final FeatureService featureService;

    public FeatureController(
            FeatureService featureService) {

        this.featureService = featureService;
    }

    @PostMapping
    public ResponseEntity<FeatureResponse>
    createFeature(
            @Valid
            @RequestBody
            FeatureCreateRequest request,
            Authentication authentication) {

        UUID userId =
                getUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        featureService.createFeature(
                                request,
                                userId
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponse>>
    getAllFeatures() {

        return ResponseEntity.ok(
                featureService.getAllFeatures()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse>
    updateFeature(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            FeatureUpdateRequest request,
            Authentication authentication) {

        UUID userId =
                getUserId(authentication);

        return ResponseEntity.ok(
                featureService.updateFeature(
                        id,
                        request,
                        userId
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<FeatureResponse>
    activateFeature(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId =
                getUserId(authentication);

        return ResponseEntity.ok(
                featureService.activateFeature(
                        id,
                        userId
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FeatureResponse>
    deactivateFeature(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId =
                getUserId(authentication);

        return ResponseEntity.ok(
                featureService.deactivateFeature(
                        id,
                        userId
                )
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

