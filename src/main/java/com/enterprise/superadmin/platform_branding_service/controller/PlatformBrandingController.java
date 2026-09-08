package com.enterprise.superadmin.platform_branding_service.controller;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.service.BrandingService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branding")
public class PlatformBrandingController {

    private final BrandingService brandingService;

    public PlatformBrandingController(
            BrandingService brandingService) {

        this.brandingService = brandingService;
    }

    @GetMapping
    public ResponseEntity<BrandingResponse> getCurrentBranding() {

        BrandingResponse response =
                brandingService.getCurrentBranding();

        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<BrandingResponse> createBranding(
            @Valid @RequestBody BrandingCreateRequest request) {

        BrandingResponse response =
                brandingService.createBranding(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PutMapping
    public ResponseEntity<BrandingResponse> updateBranding(
            @Valid @RequestBody BrandingUpdateRequest request) {

        BrandingResponse response =
                brandingService.updateBranding(request);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset")
    public ResponseEntity<BrandingResponse> resetBranding() {

        BrandingResponse response =
                brandingService.resetBranding();

        return ResponseEntity.ok(response);
    }



    @PostMapping("/preview")
    public ResponseEntity<BrandingResponse> previewBranding(
            @Valid @RequestBody BrandingUpdateRequest request) {

        BrandingResponse response =
                brandingService.previewBranding(request);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/publish")
    public ResponseEntity<BrandingResponse> publishBranding() {

        BrandingResponse response =
                brandingService.publishBranding();

        return ResponseEntity.ok(response);
    }
}