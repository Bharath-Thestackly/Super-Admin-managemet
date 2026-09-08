package servicetest;

import com.enterprise.superadmin.platform_branding_service.service.BrandingService;
import com.enterprise.superadmin.platform_branding_service.service.BrandingValidationService;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.entity.Configuration;
import com.enterprise.superadmin.platform_branding_service.exception.InvalidBrandingException;
import com.enterprise.superadmin.platform_branding_service.repository.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandingServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    private BrandingValidationService validationService;
    private BrandingService brandingService;

    @BeforeEach
    void setUp() {
        validationService = new BrandingValidationService();
        brandingService = new BrandingService(configurationRepository, validationService);
    }

    @Test
    void createBranding_savesAllFieldsAsConfigRows() {
        BrandingCreateRequest request = new BrandingCreateRequest();
        request.setBrandName("Acme Platform");
        request.setLogoUrl("https://example.com/logo.png");
        request.setPrimaryColor("#1A2B3C");
        request.setSecondaryColor("#FFFFFF");
        request.setFaviconUrl("https://example.com/favicon.ico");

        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        BrandingResponse response = brandingService.createBranding(request);

        verify(configurationRepository, times(6)).save(any(Configuration.class));
        assertNotNull(response);
    }

    @Test
    void createBranding_rejectsInvalidPrimaryColor() {
        BrandingCreateRequest request = new BrandingCreateRequest();
        request.setBrandName("Acme Platform");
        request.setPrimaryColor("not-a-color");
        request.setSecondaryColor("#FFFFFF");

        assertThrows(InvalidBrandingException.class,
                () -> brandingService.createBranding(request));

        verify(configurationRepository, never()).save(any(Configuration.class));
    }

    @Test
    void createBranding_rejectsMissingBrandName() {
        BrandingCreateRequest request = new BrandingCreateRequest();
        request.setBrandName(null);
        request.setPrimaryColor("#1A2B3C");
        request.setSecondaryColor("#FFFFFF");

        assertThrows(InvalidBrandingException.class,
                () -> brandingService.createBranding(request));
    }

    @Test
    void updateBranding_onlyUpdatesProvidedFields() {
        BrandingUpdateRequest request = new BrandingUpdateRequest();
        request.setBrandName("Updated Name");

        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        brandingService.updateBranding(request);

        verify(configurationRepository, times(1)).save(any(Configuration.class));
    }

    @Test
    void updateBranding_rejectsInvalidHexColorIfProvided() {
        BrandingUpdateRequest request = new BrandingUpdateRequest();
        request.setPrimaryColor("invalid");

        assertThrows(InvalidBrandingException.class,
                () -> brandingService.updateBranding(request));
    }

    @Test
    void resetBranding_writesDefaultValues() {
        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        brandingService.resetBranding();

        verify(configurationRepository, times(6)).save(any(Configuration.class));
    }

    @Test
    void previewBranding_doesNotPersistAnything() {
        BrandingUpdateRequest request = new BrandingUpdateRequest();
        request.setBrandName("Preview Name");

        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        BrandingResponse response = brandingService.previewBranding(request);

        assertEquals("Preview Name", response.getBrandName());
        verify(configurationRepository, never()).save(any(Configuration.class));
    }

    @Test
    void publishBranding_updatesStatusOnly() {
        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        brandingService.publishBranding();

        verify(configurationRepository, times(1)).save(any(Configuration.class));
    }

    @Test
    void getCurrentBranding_returnsExistingValues() {
        Configuration brandNameConfig = new Configuration();
        brandNameConfig.setConfigKey("branding.brand_name");
        brandNameConfig.setConfigValue("Existing Brand");

        when(configurationRepository.findByConfigKeyAndIsDeletedFalse("branding.brand_name"))
                .thenReturn(Optional.of(brandNameConfig));
        when(configurationRepository.findByConfigKeyAndIsDeletedFalse(argThat(
                key -> !key.equals("branding.brand_name"))))
                .thenReturn(Optional.empty());

        BrandingResponse response = brandingService.getCurrentBranding();

        assertEquals("Existing Brand", response.getBrandName());
    }
}
