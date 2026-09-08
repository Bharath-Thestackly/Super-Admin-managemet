package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.exception.InvalidBrandingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class BrandingValidationService {

    private static final int PLATFORM_NAME_MAX = 100;
    private static final int COMPANY_NAME_MAX = 100;
    private static final int WELCOME_MESSAGE_MAX = 250;
    private static final int FOOTER_TEXT_MAX = 200;

    private static final long LOGO_MAX_BYTES = 5L * 1024 * 1024;
    private static final long BACKGROUND_MAX_BYTES = 10L * 1024 * 1024;

    private static final List<String> LOGO_ALLOWED_TYPES =
            List.of("image/png", "image/jpeg", "image/jpg", "image/svg+xml");
    private static final List<String> BACKGROUND_ALLOWED_TYPES =
            List.of("image/png", "image/jpeg", "image/jpg");

    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#[A-Fa-f0-9]{6}$");

    private static final List<String> ALLOWED_THEMES = List.of("LIGHT", "DARK");

    public void validatePlatformName(String platformName) {
        if (platformName == null || platformName.isBlank()) {
            throw new InvalidBrandingException("platform_name is required");
        }
        if (platformName.length() > PLATFORM_NAME_MAX) {
            throw new InvalidBrandingException(
                    "platform_name must not exceed " + PLATFORM_NAME_MAX + " characters");
        }
    }

    public void validateCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new InvalidBrandingException("company_name is required");
        }
        if (companyName.length() > COMPANY_NAME_MAX) {
            throw new InvalidBrandingException(
                    "company_name must not exceed " + COMPANY_NAME_MAX + " characters");
        }
    }

    public void validateWelcomeMessage(String welcomeMessage) {
        if (welcomeMessage != null && welcomeMessage.length() > WELCOME_MESSAGE_MAX) {
            throw new InvalidBrandingException(
                    "welcome_message must not exceed " + WELCOME_MESSAGE_MAX + " characters");
        }
    }

    public void validateFooterText(String footerText) {
        if (footerText != null && footerText.length() > FOOTER_TEXT_MAX) {
            throw new InvalidBrandingException(
                    "footer_text must not exceed " + FOOTER_TEXT_MAX + " characters");
        }
    }

    public void validateHexColor(String fieldName, String colorValue) {
        if (colorValue == null || colorValue.isBlank()) {
            throw new InvalidBrandingException(fieldName + " is required");
        }
        if (!HEX_COLOR_PATTERN.matcher(colorValue).matches()) {
            throw new InvalidBrandingException(
                    fieldName + " must be a valid 6-digit hex color (e.g. #1A2B3C)");
        }
    }

    public void validateTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            throw new InvalidBrandingException("theme is required");
        }
        if (!ALLOWED_THEMES.contains(theme.toUpperCase())) {
            throw new InvalidBrandingException("theme must be one of: " + ALLOWED_THEMES);
        }
    }

    public void validateLogo(MultipartFile logo) {
        validateFile(logo, "logo", LOGO_ALLOWED_TYPES, LOGO_MAX_BYTES);
    }

    public void validateBackgroundImage(MultipartFile background) {
        validateFile(background, "background", BACKGROUND_ALLOWED_TYPES, BACKGROUND_MAX_BYTES);
    }

    public void validateFavicon(MultipartFile favicon) {
        validateFile(favicon, "favicon", LOGO_ALLOWED_TYPES, LOGO_MAX_BYTES);
    }

    public void validateEmailHeaderLogo(MultipartFile emailLogo) {
        validateFile(emailLogo, "email_header_logo", LOGO_ALLOWED_TYPES, LOGO_MAX_BYTES);
    }

    private void validateFile(MultipartFile file, String fieldName,
                              List<String> allowedTypes, long maxBytes) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new InvalidBrandingException(fieldName + " must be one of: " + allowedTypes);
        }
        if (file.getSize() > maxBytes) {
            throw new InvalidBrandingException(
                    fieldName + " exceeds max size of " + (maxBytes / (1024 * 1024)) + "MB");
        }
    }
}