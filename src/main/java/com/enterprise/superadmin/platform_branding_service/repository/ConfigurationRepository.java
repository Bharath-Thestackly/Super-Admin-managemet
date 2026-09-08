package com.enterprise.superadmin.platform_branding_service.repository;

import com.enterprise.superadmin.platform_branding_service.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationRepository
        extends JpaRepository<Configuration, UUID> {

    Optional<Configuration> findByConfigKeyAndIsDeletedFalse(
            String configKey
    );

    List<Configuration> findByCategoryAndIsDeletedFalse(
            String category
    );

    List<Configuration> findByCategoryAndScopeAndIsDeletedFalse(
            String category,
            String scope
    );

    boolean existsByConfigKeyAndIsDeletedFalse(
            String configKey
    );
}