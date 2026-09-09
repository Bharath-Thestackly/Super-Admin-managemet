package com.enterprise.superadmin.platform_branding_service;


import com.enterprise.superadmin.platform_branding_service.service.BrandingValidationService;
import com.enterprise.superadmin.platform_branding_service.exception.InvalidBrandingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class BrandingValidationServiceTest {

    private final BrandingValidationService validationService = new BrandingValidationService();

    @Test
    void validatePlatformName_acceptsValidName() {
        assertDoesNotThrow(() -> validationService.validatePlatformName("My Platform"));
    }

    @Test
    void validatePlatformName_rejectsNull() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validatePlatformName(null));
    }

    @Test
    void validatePlatformName_rejectsBlank() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validatePlatformName("   "));
    }

    @Test
    void validatePlatformName_rejectsOver100Chars() {
        String tooLong = "a".repeat(101);
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validatePlatformName(tooLong));
    }

    @Test
    void validatePlatformName_acceptsExactly100Chars() {
        String exact = "a".repeat(100);
        assertDoesNotThrow(() -> validationService.validatePlatformName(exact));
    }

    @Test
    void validateCompanyName_rejectsNull() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateCompanyName(null));
    }

    @Test
    void validateWelcomeMessage_allowsNull() {
        assertDoesNotThrow(() -> validationService.validateWelcomeMessage(null));
    }

    @Test
    void validateWelcomeMessage_rejectsOver250Chars() {
        String tooLong = "a".repeat(251);
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateWelcomeMessage(tooLong));
    }

    @Test
    void validateFooterText_rejectsOver200Chars() {
        String tooLong = "a".repeat(201);
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateFooterText(tooLong));
    }

    @ParameterizedTest
    @ValueSource(strings = {"#1A2B3C", "#FFFFFF", "#000000", "#abcdef"})
    void validateHexColor_acceptsValidHex(String color) {
        assertDoesNotThrow(() -> validationService.validateHexColor("primary_color", color));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1A2B3C", "#GGGGGG", "#12345", "red", "#1234567"})
    void validateHexColor_rejectsInvalidHex(String color) {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateHexColor("primary_color", color));
    }

    @Test
    void validateHexColor_rejectsNull() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateHexColor("primary_color", null));
    }

    @Test
    void validateTheme_acceptsLight() {
        assertDoesNotThrow(() -> validationService.validateTheme("LIGHT"));
    }

    @Test
    void validateTheme_acceptsDarkCaseInsensitive() {
        assertDoesNotThrow(() -> validationService.validateTheme("dark"));
    }

    @Test
    void validateTheme_rejectsInvalidValue() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateTheme("BLUE"));
    }

    @Test
    void validateTheme_rejectsNull() {
        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateTheme(null));
    }

    @Test
    void validateLogo_allowsNullFile() {
        assertDoesNotThrow(() -> validationService.validateLogo(null));
    }

    @Test
    void validateLogo_acceptsValidPng() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getContentType()).thenReturn("image/png");
        Mockito.when(file.getSize()).thenReturn(1024L * 1024);

        assertDoesNotThrow(() -> validationService.validateLogo(file));
    }

    @Test
    void validateLogo_rejectsOversizedFile() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getContentType()).thenReturn("image/png");
        Mockito.when(file.getSize()).thenReturn(6L * 1024 * 1024);

        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateLogo(file));
    }

    @Test
    void validateLogo_rejectsWrongFileType() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getContentType()).thenReturn("application/pdf");
        Mockito.when(file.getSize()).thenReturn(1024L);

        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateLogo(file));
    }

    @Test
    void validateBackgroundImage_rejectsOversizedFile() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getContentType()).thenReturn("image/jpeg");
        Mockito.when(file.getSize()).thenReturn(11L * 1024 * 1024);

        assertThrows(InvalidBrandingException.class,
                () -> validationService.validateBackgroundImage(file));
    }
}