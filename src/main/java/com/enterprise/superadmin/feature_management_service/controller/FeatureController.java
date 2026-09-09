package com.enterprise.superadmin.feature_management_service.controller;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureCreateRequest;
import com.enterprise.superadmin.feature_management_service.dto.request.FeatureUpdateRequest;
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

        if (request.getCreatedBy() == null) {
            request.setCreatedBy(userId.toString());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        featureService.createFeature(
                                request
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

        if (request.getUpdatedBy() == null) {
            request.setUpdatedBy(userId.toString());
        }

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

        return ResponseEntity.ok(
                featureService.enableFeature(
                        id
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FeatureResponse>
    deactivateFeature(
            @PathVariable UUID id,
            Authentication authentication) {

        return ResponseEntity.ok(
                featureService.disableFeature(
                        id
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
