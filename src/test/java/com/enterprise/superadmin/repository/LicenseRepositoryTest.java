package com.enterprise.superadmin.repository;


import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseRepositoryTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Test
    void shouldFindLicenseById() {

        UUID id = UUID.randomUUID();

        License license = new License();

        license.setId(id);
        license.setLicenseKey("LIC-TEST123");
        license.setLicensePlan("PREMIUM");
        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );
        license.setActivationDate(
                LocalDate.now()
        );
        license.setExpiryDate(
                LocalDate.now().plusDays(365)
        );
        license.setStatus(
                LicenseStatus.ACTIVE
        );

        when(
                licenseRepository.findById(id)
        ).thenReturn(
                Optional.of(license)
        );

        Optional<License> result =
                licenseRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(
                id,
                result.get().getId()
        );
        assertEquals(
                "LIC-TEST123",
                result.get().getLicenseKey()
        );

        verify(
                licenseRepository
        ).findById(id);
    }

    @Test
    void shouldCheckLicenseKeyExists() {

        String licenseKey =
                "LIC-TEST123";

        when(
                licenseRepository
                        .existsByLicenseKey(licenseKey)
        ).thenReturn(true);

        boolean result =
                licenseRepository
                        .existsByLicenseKey(licenseKey);

        assertTrue(result);

        verify(
                licenseRepository
        ).existsByLicenseKey(licenseKey);
    }

    @Test
    void shouldSearchLicenses() {

        License license = new License();

        license.setLicenseKey(
                "LIC-TEST123"
        );

        license.setLicensePlan(
                "PREMIUM"
        );

        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );

        license.setActivationDate(
                LocalDate.now()
        );

        license.setExpiryDate(
                LocalDate.now().plusDays(365)
        );

        license.setStatus(
                LicenseStatus.ACTIVE
        );

        when(
                licenseRepository.searchLicenses(
                        LicenseStatus.ACTIVE,
                        LicenseType.SUBSCRIPTION,
                        "PREMIUM"
                )
        ).thenReturn(
                List.of(license)
        );

        List<License> result =
                licenseRepository.searchLicenses(
                        LicenseStatus.ACTIVE,
                        LicenseType.SUBSCRIPTION,
                        "PREMIUM"
                );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "PREMIUM",
                result.get(0).getLicensePlan()
        );
    }
}