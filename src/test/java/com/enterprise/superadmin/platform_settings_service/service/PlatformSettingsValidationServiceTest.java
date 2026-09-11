package com.enterprise.superadmin.platform_settings_service.service;

import com.enterprise.superadmin.platform_settings_service.dto.request.UpdatePlatformSettingsRequest;
import com.enterprise.superadmin.platform_settings_service.exception.InvalidSettingException;
import com.enterprise.superadmin.platform_settings_service.service.validation.PlatformSettingsValidationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformSettingsValidationServiceTest {

    private final PlatformSettingsValidationService validationService =
            new PlatformSettingsValidationService();

    @Test
    void shouldAcceptValidSettings() {
        UpdatePlatformSettingsRequest request = validRequest();
        assertDoesNotThrow(() -> validationService.validate(request));
    }

    @Test
    void shouldRejectInvalidPasswordExpiry() {
        UpdatePlatformSettingsRequest request = validRequest();
        request.setPasswordExpiry(10);
        assertThrows(InvalidSettingException.class, () -> validationService.validate(request));
    }

    @Test
    void shouldRejectInvalidSessionTimeout() {
        UpdatePlatformSettingsRequest request = validRequest();
        request.setSessionTimeout(300);
        assertThrows(InvalidSettingException.class, () -> validationService.validate(request));
    }

    @Test
    void shouldRejectInvalidMaximumLoginAttempts() {
        UpdatePlatformSettingsRequest request = validRequest();
        request.setMaximumLoginAttempts(2);
        assertThrows(InvalidSettingException.class, () -> validationService.validate(request));
    }

    @Test
    void shouldRejectInvalidFileUploadSize() {
        UpdatePlatformSettingsRequest request = validRequest();
        request.setMaximumFileUploadSize(0L);
        assertThrows(InvalidSettingException.class, () -> validationService.validate(request));
    }

    private UpdatePlatformSettingsRequest validRequest() {
        UpdatePlatformSettingsRequest request = new UpdatePlatformSettingsRequest();
        request.setDefaultLanguage("English");
        request.setDefaultTimeZone("Asia/Kolkata");
        request.setDefaultCurrency("INR");
        request.setDateFormat("DD/MM/YYYY");
        request.setTimeFormat("24 Hours");
        request.setNumberFormat("#,##0.00");
        request.setSessionTimeout(30);
        request.setAutoLogout(true);
        request.setPasswordExpiry(90);
        request.setMaximumLoginAttempts(5);
        request.setMaintenanceNotification(false);
        request.setSystemAnnouncement(true);
        request.setMultiFactorAuthentication(true);
        request.setEmailNotifications(true);
        request.setSmsNotifications(true);
        request.setPushNotifications(true);
        request.setMaximumFileUploadSize(100L);
        request.setDefaultTheme("Light");
        request.setMaintenanceMode(false);
        return request;
    }

}