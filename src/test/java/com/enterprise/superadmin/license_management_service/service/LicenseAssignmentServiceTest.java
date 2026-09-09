package com.enterprise.superadmin.license_management_service.service;


import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicenseAssignmentServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseAssignmentRepository assignmentRepository;

    @Mock
    private TenantClient tenantClient;

    @InjectMocks
    private LicenseAssignmentServiceImpl assignmentService;

    @Test
    void shouldAssignLicense() {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        License license =
                createActiveLicense();

        when(
                licenseRepository.findById(licenseId)
        ).thenReturn(
                java.util.Optional.of(license)
        );

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        ).thenReturn(false);

        when(
                tenantClient.getTenant(tenantId)
        ).thenReturn(
                new TenantResponse(
                        tenantId,
                        "Test Tenant",
                        UUID.randomUUID(),
                        "ACTIVE"
                )
        );

        assignmentService.assignLicense(
                licenseId,
                new LicenseAssignmentRequest(
                        tenantId
                )
        );

        verify(
                assignmentRepository
        ).save(any());
    }

    @Test
    void shouldRejectAlreadyAssignedLicense() {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        when(
                licenseRepository.findById(licenseId)
        ).thenReturn(
                java.util.Optional.of(
                        createActiveLicense()
                )
        );

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        ).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () ->
                        assignmentService.assignLicense(
                                licenseId,
                                new LicenseAssignmentRequest(
                                        tenantId
                                )
                        )
        );

        verify(
                assignmentRepository,
                never()
        ).save(any());
    }

    private License createActiveLicense() {

        License license =
                new License();

        license.setId(
                UUID.randomUUID()
        );

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

        return license;
    }
}