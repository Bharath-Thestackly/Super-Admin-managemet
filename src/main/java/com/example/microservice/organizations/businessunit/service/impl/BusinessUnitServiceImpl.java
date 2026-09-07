package com.example.microservice.organizations.businessunit.service.impl;

import com.example.microservice.common.exception.BadRequestException;
import com.example.microservice.common.exception.ResourceNotFoundException;
import com.example.microservice.common.tenant.TenantContext;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.microservice.organizations.businessunit.entity.BusinessUnit;
import com.example.microservice.organizations.businessunit.repository.BusinessUnitRepository;
import com.example.microservice.organizations.businessunit.service.BusinessUnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessUnitServiceImpl implements BusinessUnitService {

    private final BusinessUnitRepository repository;

    public BusinessUnitServiceImpl(BusinessUnitRepository repository) {
        this.repository = repository;
    }

    /**
     * Extracts the active user from Spring Security context.
     * Falls back to "ADMIN" if unauthenticated.
     */
    private String resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            if (auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                return auth.getName();
            }
        }
        return "ADMIN";
    }

    // =========================================================================
    // BaseService Implementation: create
    // =========================================================================
    @Override
    @Transactional
    public BusinessUnitResponseDto create(BusinessUnitRequestDto request) {
        if (repository.existsByUnitCodeAndIsDeletedFalse(request.getUnitCode())) {
            throw new BadRequestException("Business Unit code already exists: " + request.getUnitCode());
        }

        String currentUser = resolveCurrentUser();
        String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || currentTenant.isBlank()) {
            currentTenant = "default";
        }

        BusinessUnit entity = BusinessUnit.builder()
                .unitName(request.getUnitName())
                .unitCode(request.getUnitCode())
                .description(request.getDescription())
                .organizationId(request.getOrganizationId())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .createdAt(LocalDateTime.now())
                .createdBy(currentUser)
                .updatedAt(LocalDateTime.now())
                .updatedBy(currentUser)
                .tenantId(currentTenant)
                .isDeleted(false)
                .build();

        return mapToDto(repository.save(entity));
    }

    // =========================================================================
    // BaseService Implementation: getById
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public BusinessUnitResponseDto getById(Long id) {
        BusinessUnit entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));
        return mapToDto(entity);
    }

    // =========================================================================
    // BaseService Implementation: getAll (Unpaged)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<BusinessUnitResponseDto> getAll() {
        return repository.findByIsDeletedFalse().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // BaseService Implementation: getAll (Paged)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public Page<BusinessUnitResponseDto> getAll(Pageable pageable) {
        return repository.findByIsDeletedFalse(pageable).map(this::mapToDto);
    }

    // =========================================================================
    // BaseService Implementation: update
    // =========================================================================
    @Override
    @Transactional
    public BusinessUnitResponseDto update(Long id, BusinessUnitRequestDto request) {
        BusinessUnit entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));

        String currentUser = resolveCurrentUser();

        entity.setUnitName(request.getUnitName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(currentUser);

        return mapToDto(repository.save(entity));
    }

    // =========================================================================
    // BaseService Implementation: deleteById (Soft Delete)
    // =========================================================================
    @Override
    @Transactional
    public void deleteById(Long id) {
        BusinessUnit entity = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("BusinessUnit", "id", id));

        String currentUser = resolveCurrentUser();

        entity.setIsDeleted(true);
        entity.setDeletedAt(LocalDate.now());
        entity.setDeletedBy(currentUser);
        repository.save(entity);
    }

    // =========================================================================
    // BaseService Implementation: existsById
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.findByIdAndIsDeletedFalse(id).isPresent();
    }

    // =========================================================================
    // Custom Operation: searchBusinessUnits (Case-insensitive keyword search)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<BusinessUnitResponseDto> searchBusinessUnits(String query) {
        return repository.searchBusinessUnits(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BusinessUnitResponseDto mapToDto(BusinessUnit unit) {
        return BusinessUnitResponseDto.builder()
                .id(unit.getId())
                .unitName(unit.getUnitName())
                .unitCode(unit.getUnitCode())
                .description(unit.getDescription())
                .organizationId(unit.getOrganizationId())
                .status(unit.getStatus())
                .createdAt(unit.getCreatedAt())
                .createdBy(unit.getCreatedBy())
                .updatedAt(unit.getUpdatedAt())
                .updatedBy(unit.getUpdatedBy())
                .build();
    }
}
