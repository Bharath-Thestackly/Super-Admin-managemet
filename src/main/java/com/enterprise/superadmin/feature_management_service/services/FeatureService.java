package com.enterprise.superadmin.feature_management_service.services;

import com.enterprise.publicadmin.feature.dto.request.FeatureCreateRequest;
import com.enterprise.publicadmin.feature.dto.request.FeatureUpdateRequest;
import com.enterprise.publicadmin.feature.dto.response.FeatureResponse;
import com.enterprise.publicadmin.feature.entity.Feature;
import com.enterprise.publicadmin.feature.exception.FeatureConfigurationException;
import com.enterprise.publicadmin.feature.exception.FeatureNotFoundException;
import com.enterprise.publicadmin.feature.exception.InvalidFeatureStateException;
import com.enterprise.publicadmin.feature.repository.FeatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeatureService {

    private final FeatureRepository featureRepository;

    public FeatureService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    // CREATE

    public FeatureResponse createFeature(FeatureCreateRequest request) {

        if (featureRepository.existsByFeatureName(request.getFeatureName())) {
            throw new InvalidFeatureStateException(
                    "Feature already exists: " + request.getFeatureName()
            );
        }

        validateStatus(request.getStatus());

        Feature feature = new Feature();

        feature.setFeatureName(request.getFeatureName());
        feature.setModule(request.getModule());
        feature.setLicensePlan(request.getLicensePlan());
        feature.setStatus(request.getStatus());

        validateConfiguration(request.getConfiguration());
        feature.setConfiguration(request.getConfiguration());

        if (request.getCreatedBy() != null) {
            feature.setCreatedBy(
                    UUID.fromString(request.getCreatedBy())
            );
        }

        Feature savedFeature = featureRepository.save(feature);

        return mapToResponse(savedFeature);
    }

    // GET ALL

    @Transactional(readOnly = true)
    public List<FeatureResponse> getAllFeatures() {

        return featureRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID

    @Transactional(readOnly = true)
    public FeatureResponse getFeatureById(UUID id) {

        Feature feature = featureRepository.findById(id)
                .orElseThrow(() ->
                        new FeatureNotFoundException(
                                "Feature not found with id: " + id
                        )
                );

        return mapToResponse(feature);
    }

    // UPDATE

    public FeatureResponse updateFeature(
            UUID id,
            FeatureUpdateRequest request) {

        Feature feature = featureRepository.findById(id)
                .orElseThrow(() ->
                        new FeatureNotFoundException(
                                "Feature not found with id: " + id
                        )
                );

        if (request.getFeatureName() != null) {
            feature.setFeatureName(request.getFeatureName());
        }

        if (request.getModule() != null) {
            feature.setModule(request.getModule());
        }

        if (request.getLicensePlan() != null) {
            feature.setLicensePlan(request.getLicensePlan());
        }

        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            feature.setStatus(request.getStatus());
        }

        if (request.getConfiguration() != null) {
            validateConfiguration(request.getConfiguration());
            feature.setConfiguration(request.getConfiguration());
        }

        if (request.getUpdatedBy() != null) {
            feature.setUpdatedBy(
                    UUID.fromString(request.getUpdatedBy())
            );
        }

        Feature updatedFeature = featureRepository.save(feature);

        return mapToResponse(updatedFeature);
    }

    // ENABLE

    public FeatureResponse enableFeature(UUID id) {

        Feature feature = getFeatureEntity(id);

        if ("ENABLED".equals(feature.getStatus())) {
            throw new InvalidFeatureStateException(
                    "Feature is already enabled"
            );
        }

        feature.setStatus("ENABLED");

        return mapToResponse(
                featureRepository.save(feature)
        );
    }

    // DISABLE

    public FeatureResponse disableFeature(UUID id) {

        Feature feature = getFeatureEntity(id);

        if ("DISABLED".equals(feature.getStatus())) {
            throw new InvalidFeatureStateException(
                    "Feature is already disabled"
            );
        }

        feature.setStatus("DISABLED");

        return mapToResponse(
                featureRepository.save(feature)
        );
    }

    // DELETE

    public void deleteFeature(UUID id) {

        Feature feature = getFeatureEntity(id);

        featureRepository.delete(feature);
    }

    // FIND ENTITY

    private Feature getFeatureEntity(UUID id) {

        return featureRepository.findById(id)
                .orElseThrow(() ->
                        new FeatureNotFoundException(
                                "Feature not found with id: " + id
                        )
                );
    }

    // STATUS VALIDATION

    private void validateStatus(String status) {

        if (!"ENABLED".equals(status)
                && !"DISABLED".equals(status)) {

            throw new InvalidFeatureStateException(
                    "Status must be ENABLED or DISABLED"
            );
        }
    }

    // CONFIGURATION VALIDATION

    private void validateConfiguration(String configuration) {

        if (configuration == null ||
                configuration.trim().isEmpty()) {
            return;
        }

        if (!configuration.trim().startsWith("{")
                || !configuration.trim().endsWith("}")) {

            throw new FeatureConfigurationException(
                    "Configuration must be a valid JSON object"
            );
        }
    }

    // ENTITY → DTO

    private FeatureResponse mapToResponse(Feature feature) {

        FeatureResponse response = new FeatureResponse();

        response.setId(feature.getId());
        response.setFeatureName(feature.getFeatureName());
        response.setModule(feature.getModule());
        response.setLicensePlan(feature.getLicensePlan());
        response.setStatus(feature.getStatus());
        response.setConfiguration(feature.getConfiguration());
        response.setCreatedAt(feature.getCreatedAt());
        response.setUpdatedAt(feature.getUpdatedAt());
        response.setCreatedBy(feature.getCreatedBy());
        response.setUpdatedBy(feature.getUpdatedBy());

        return response;
    }
}