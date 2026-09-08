package com.enterprise.superadmin.platform_branding_service.repository;

import com.enterprise.superadmin.platform_branding_service.entity.PlatformBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlatformBrandingRepository
        extends JpaRepository<PlatformBranding, UUID> {

    Optional<PlatformBranding> findByIdAndIsDeletedFalse(UUID id);

    Optional<PlatformBranding> findFirstByIsDeletedFalse();

    boolean existsByIsDeletedFalse();
}