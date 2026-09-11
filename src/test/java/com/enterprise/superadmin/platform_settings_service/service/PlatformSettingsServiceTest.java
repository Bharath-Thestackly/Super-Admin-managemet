package com.enterprise.superadmin.platform_settings_service.service;

import com.enterprise.superadmin.platform_settings_service.dto.response.PlatformSettingsResponse;
import com.enterprise.superadmin.platform_settings_service.entity.PlatformSetting;
import com.enterprise.superadmin.platform_settings_service.enums.SettingStatus;
import com.enterprise.superadmin.platform_settings_service.integration.PlatformSettingsAuditIntegration;
import com.enterprise.superadmin.platform_settings_service.integration.PlatformSettingsPropagationIntegration;
import com.enterprise.superadmin.platform_settings_service.mapper.PlatformSettingsMapper;
import com.enterprise.superadmin.platform_settings_service.repository.PlatformSettingHistoryRepository;
import com.enterprise.superadmin.platform_settings_service.repository.PlatformSettingRepository;
import com.enterprise.superadmin.platform_settings_service.security.CurrentUserProvider;
import com.enterprise.superadmin.platform_settings_service.service.impl.PlatformSettingsServiceImpl;
import com.enterprise.superadmin.platform_settings_service.service.validation.PlatformSettingsValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlatformSettingsServiceTest {

    @Mock
    private PlatformSettingRepository settingRepository;

    @Mock
    private PlatformSettingHistoryRepository historyRepository;

    @Mock
    private PlatformSettingsMapper mapper;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PlatformSettingsValidationService validationService;

    @Mock
    private PlatformSettingsPropagationIntegration propagationIntegration;

    @Mock
    private PlatformSettingsAuditIntegration auditIntegration;

    @InjectMocks
    private PlatformSettingsServiceImpl service;

    private PlatformSetting setting;

    @BeforeEach
    void setUp() {

        setting = PlatformSetting.builder()
                .id(UUID.randomUUID())
                .settingName("GLOBAL_SETTINGS")
                .category("GLOBAL")
                .status(SettingStatus.ACTIVE)
                .defaultLanguage("English")
                .defaultTimeZone("Asia/Kolkata")
                .defaultCurrency("INR")
                .dateFormat("DD/MM/YYYY")
                .timeFormat("24 Hours")
                .numberFormat("#,##0.00")
                .sessionTimeout(30)
                .autoLogout(true)
                .passwordExpiry(90)
                .maximumLoginAttempts(5)
                .maintenanceNotification(false)
                .systemAnnouncement(true)
                .multiFactorAuthentication(true)
                .emailNotifications(true)
                .smsNotifications(true)
                .pushNotifications(true)
                .maximumFileUploadSize(100L)
                .defaultTheme("Light")
                .maintenanceMode(false)
                .versionNumber(1L)
                .build();
    }

    @Test
    void shouldGetSetting() {

        when(settingRepository.findBySettingName("GLOBAL_SETTINGS"))
                .thenReturn(Optional.of(setting));

        PlatformSettingsResponse response = PlatformSettingsResponse.builder()
                .id(setting.getId())
                .settingName("GLOBAL_SETTINGS")
                .build();

        when(mapper.toResponse(setting))
                .thenReturn(response);

        PlatformSettingsResponse result = service.getSetting("GLOBAL_SETTINGS");

        assertNotNull(result);
        assertEquals("GLOBAL_SETTINGS", result.getSettingName());

        verify(settingRepository).findBySettingName("GLOBAL_SETTINGS");
    }

}