package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.entity.Configuration;
import com.enterprise.superadmin.platform_branding_service.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BrandingService {

    private static final String CATEGORY = "BRANDING";
    private static final String SCOPE = "PLATFORM";

    private static final String KEY_BRAND_NAME = "branding.brand_name";
    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_PRIMARY_COLOR = "branding.primary_color";
    private static final String KEY_SECONDARY_COLOR = "branding.secondary_color";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_STATUS = "branding.status";

    private final ConfigurationRepository configurationRepository;
    private final BrandingValidationService validationService;

    public BrandingService(ConfigurationRepository configurationRepository,
                           BrandingValidationService validationService) {
        this.configurationRepository = configurationRepository;
        this.validationService = validationService;
    }

    @Transactional(readOnly = true)
    public BrandingResponse getCurrentBranding() {
        return buildResponse();
    }

    @Transactional
    public BrandingResponse createBranding(BrandingCreateRequest request) {
        validationService.validatePlatformName(request.getBrandName());
        validationService.validateHexColor("primary_color", request.getPrimaryColor());
        validationService.validateHexColor("secondary_color", request.getSecondaryColor());

        String actor = "system"; // TODO: replace with authenticated user once JWT/security is wired in

        upsert(KEY_BRAND_NAME, request.getBrandName(), actor);
        upsert(KEY_LOGO_URL, request.getLogoUrl(), actor);
        upsert(KEY_PRIMARY_COLOR, request.getPrimaryColor(), actor);
        upsert(KEY_SECONDARY_COLOR, request.getSecondaryColor(), actor);
        upsert(KEY_FAVICON_URL, request.getFaviconUrl(), actor);
        upsert(KEY_STATUS, "DRAFT", actor);

        return buildResponse();
    }

    @Transactional
    public BrandingResponse updateBranding(BrandingUpdateRequest request) {
        String actor = "system"; // TODO: replace with authenticated user

        if (request.getBrandName() != null) {
            validationService.validatePlatformName(request.getBrandName());
            upsert(KEY_BRAND_NAME, request.getBrandName(), actor);
        }
        if (request.getLogoUrl() != null) {
            upsert(KEY_LOGO_URL, request.getLogoUrl(), actor);
        }
        if (request.getPrimaryColor() != null) {
            validationService.validateHexColor("primary_color", request.getPrimaryColor());
            upsert(KEY_PRIMARY_COLOR, request.getPrimaryColor(), actor);
        }
        if (request.getSecondaryColor() != null) {
            validationService.validateHexColor("secondary_color", request.getSecondaryColor());
            upsert(KEY_SECONDARY_COLOR, request.getSecondaryColor(), actor);
        }
        if (request.getFaviconUrl() != null) {
            upsert(KEY_FAVICON_URL, request.getFaviconUrl(), actor);
        }

        return buildResponse();
    }

    @Transactional
    public BrandingResponse resetBranding() {
        String actor = "system";
        upsert(KEY_BRAND_NAME, "Platform", actor);
        upsert(KEY_LOGO_URL, null, actor);
        upsert(KEY_PRIMARY_COLOR, "#0052CC", actor);
        upsert(KEY_SECONDARY_COLOR, "#172B4D", actor);
        upsert(KEY_FAVICON_URL, null, actor);
        upsert(KEY_STATUS, "DRAFT", actor);
        return buildResponse();
    }

    @Transactional(readOnly = true)
    public BrandingResponse previewBranding(BrandingUpdateRequest request) {
        if (request.getBrandName() != null) validationService.validatePlatformName(request.getBrandName());
        if (request.getPrimaryColor() != null) validationService.validateHexColor("primary_color", request.getPrimaryColor());
        if (request.getSecondaryColor() != null) validationService.validateHexColor("secondary_color", request.getSecondaryColor());

        BrandingResponse current = buildResponse();
        return new BrandingResponse(
                current.getId(),
                request.getBrandName() != null ? request.getBrandName() : current.getBrandName(),
                request.getLogoUrl() != null ? request.getLogoUrl() : current.getLogoUrl(),
                request.getPrimaryColor() != null ? request.getPrimaryColor() : current.getPrimaryColor(),
                request.getSecondaryColor() != null ? request.getSecondaryColor() : current.getSecondaryColor(),
                request.getFaviconUrl() != null ? request.getFaviconUrl() : current.getFaviconUrl(),
                current.getStatus()
        );
    }

    @Transactional
    public BrandingResponse publishBranding() {
        upsert(KEY_STATUS, "PUBLISHED", "system"); // TODO: actual actor once security is wired in
        return buildResponse();
    }

    // ---------- helpers ----------

    private void upsert(String key, String value, String actor) {
        Optional<Configuration> existing = configurationRepository.findByConfigKeyAndIsDeletedFalse(key);
        Configuration config = existing.orElseGet(Configuration::new);
        boolean isNew = existing.isEmpty();

        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setCategory(CATEGORY);
        config.setScope(SCOPE);
        config.setStatus("ACTIVE");
        config.setIsDeleted(false);
        config.setUpdatedAt(LocalDate.now());
        config.setUpdatedBy(actor);
        if (isNew) {
            config.setCreatedAt(LocalDate.now());
            config.setCreatedBy(actor);
        }
        configurationRepository.save(config);
    }

    private String get(String key) {
        return configurationRepository.findByConfigKeyAndIsDeletedFalse(key)
                .map(Configuration::getConfigValue)
                .orElse(null);
    }

    private BrandingResponse buildResponse() {
        return new BrandingResponse(
                null, // TODO: id type mismatch (Long vs UUID) — flag to Member 1
                get(KEY_BRAND_NAME),
                get(KEY_LOGO_URL),
                get(KEY_PRIMARY_COLOR),
                get(KEY_SECONDARY_COLOR),
                get(KEY_FAVICON_URL),
                get(KEY_STATUS)
        );
    }
}