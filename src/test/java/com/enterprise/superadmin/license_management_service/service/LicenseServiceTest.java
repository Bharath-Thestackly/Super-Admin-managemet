package com.enterprise.superadmin.license_management_service.service;


import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseAssignmentRepository assignmentRepository;

    @Mock
    private LicenseValidationService validationService;

    @InjectMocks
    private LicenseServiceImpl licenseService;

    private UUID actorId;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
    }

    @Test
    void shouldCreateLicense() {

        LicenseCreateRequest request =
                new LicenseCreateRequest(
                        "ENTERPRISE",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusYears(1)
                );

        when(licenseRepository.existsByLicenseKey(any()))
                .thenReturn(false);

        when(licenseRepository.save(any(License.class)))
                .thenAnswer(invocation -> {

                    License license =
                            invocation.getArgument(0);

                    license.setId(UUID.randomUUID());

                    return license;
                });

        LicenseResponse response =
                licenseService.createLicense(
                        request,
                        actorId
                );

        assertNotNull(response);
        assertNotNull(response.licenseKey());
        assertEquals(
                "ENTERPRISE",
                response.licensePlan()
        );
        assertEquals(
                LicenseStatus.PENDING,
                response.status()
        );

        verify(licenseRepository)
                .save(any(License.class));
    }

    @Test
    void shouldActivateLicense() {

        UUID licenseId = UUID.randomUUID();

        License license = new License();

        license.setId(licenseId);
        license.setLicenseKey("LIC-TEST");
        license.setLicensePlan("ENTERPRISE");
        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );
        license.setActivationDate(LocalDate.now());
        license.setExpiryDate(
                LocalDate.now().plusYears(1)
        );
        license.setStatus(
                LicenseStatus.PENDING
        );

        when(licenseRepository.findById(licenseId))
                .thenReturn(java.util.Optional.of(license));

        when(licenseRepository.save(any(License.class)))
                .thenReturn(license);

        LicenseResponse response =
                licenseService.activateLicense(
                        licenseId,
                        actorId
                );

        assertEquals(
                LicenseStatus.ACTIVE,
                response.status()
        );
    }

    @Test
    void shouldSuspendActiveLicense() {

        UUID licenseId = UUID.randomUUID();

        License license = new License();

        license.setId(licenseId);
        license.setLicenseKey("LIC-TEST");
        license.setLicensePlan("ENTERPRISE");
        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );
        license.setActivationDate(LocalDate.now());
        license.setExpiryDate(
                LocalDate.now().plusYears(1)
        );
        license.setStatus(
                LicenseStatus.ACTIVE
        );

        when(licenseRepository.findById(licenseId))
                .thenReturn(java.util.Optional.of(license));

        when(licenseRepository.save(any(License.class)))
                .thenReturn(license);

        LicenseResponse response =
                licenseService.suspendLicense(
                        licenseId,
                        actorId
                );

        assertEquals(
                LicenseStatus.SUSPENDED,
                response.status()
        );
    }
}

