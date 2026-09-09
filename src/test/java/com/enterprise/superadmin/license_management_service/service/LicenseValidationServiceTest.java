package com.enterprise.superadmin.license_management_service.service;


import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.exception.InvalidLicenseStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LicenseValidationServiceTest {

    private LicenseValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService =
                new LicenseValidationServiceImpl();
    }

    @Test
    void shouldValidateCreateRequest() {

        LicenseCreateRequest request =
                new LicenseCreateRequest(
                        "PREMIUM",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusDays(365)
                );

        assertDoesNotThrow(() ->
                validationService.validateCreate(request)
        );
    }

    @Test
    void shouldRejectCreateWhenExpiryIsBeforeActivation() {

        LicenseCreateRequest request =
                new LicenseCreateRequest(
                        "PREMIUM",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now().plusDays(10),
                        LocalDate.now()
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCreate(request)
        );
    }

    @Test
    void shouldRejectCreateWhenExpiryEqualsActivation() {

        LocalDate date = LocalDate.now();

        LicenseCreateRequest request =
                new LicenseCreateRequest(
                        "PREMIUM",
                        LicenseType.SUBSCRIPTION,
                        date,
                        date
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCreate(request)
        );
    }

    @Test
    void shouldValidateUpdateRequest() {

        LicenseUpdateRequest request =
                new LicenseUpdateRequest(
                        "STANDARD",
                        LicenseType.SOFTWARE,
                        LocalDate.now(),
                        LocalDate.now().plusDays(180)
                );

        assertDoesNotThrow(() ->
                validationService.validateUpdate(request)
        );
    }

    @Test
    void shouldRejectInvalidUpdateDates() {

        LicenseUpdateRequest request =
                new LicenseUpdateRequest(
                        "STANDARD",
                        LicenseType.SOFTWARE,
                        LocalDate.now().plusDays(100),
                        LocalDate.now()
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateUpdate(request)
        );
    }

    @Test
    void shouldValidateRenewal() {

        LocalDate currentExpiry =
                LocalDate.now().plusDays(30);

        LicenseRenewRequest request =
                new LicenseRenewRequest(
                        LocalDate.now().plusDays(365)
                );

        assertDoesNotThrow(() ->
                validationService.validateRenewal(
                        LicenseStatus.ACTIVE,
                        currentExpiry,
                        request
                )
        );
    }

    @Test
    void shouldRejectRenewalWithEarlierExpiry() {

        LocalDate currentExpiry =
                LocalDate.now().plusDays(365);

        LicenseRenewRequest request =
                new LicenseRenewRequest(
                        LocalDate.now().plusDays(30)
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () ->
                        validationService.validateRenewal(
                                LicenseStatus.ACTIVE,
                                currentExpiry,
                                request
                        )
        );
    }

    @Test
    void shouldRejectRenewalForSuspendedLicense() {

        LocalDate currentExpiry =
                LocalDate.now().plusDays(30);

        LicenseRenewRequest request =
                new LicenseRenewRequest(
                        LocalDate.now().plusDays(365)
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () ->
                        validationService.validateRenewal(
                                LicenseStatus.SUSPENDED,
                                currentExpiry,
                                request
                        )
        );
    }

    @Test
    void shouldAllowPendingToActive() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.PENDING,
                        LicenseStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldAllowActiveToSuspended() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.ACTIVE,
                        LicenseStatus.SUSPENDED
                )
        );
    }

    @Test
    void shouldAllowSuspendedToActive() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.SUSPENDED,
                        LicenseStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldRejectPendingToSuspended() {

        assertThrows(
                InvalidLicenseStateException.class,
                () ->
                        validationService.validateTransition(
                                LicenseStatus.PENDING,
                                LicenseStatus.SUSPENDED
                        )
        );
    }

    @Test
    void shouldRejectExpiredToActive() {

        assertThrows(
                InvalidLicenseStateException.class,
                () ->
                        validationService.validateTransition(
                                LicenseStatus.EXPIRED,
                                LicenseStatus.ACTIVE
                        )
        );
    }

    @Test
    void shouldRejectActiveToPending() {

        assertThrows(
                InvalidLicenseStateException.class,
                () ->
                        validationService.validateTransition(
                                LicenseStatus.ACTIVE,
                                LicenseStatus.PENDING
                        )
        );
    }
}