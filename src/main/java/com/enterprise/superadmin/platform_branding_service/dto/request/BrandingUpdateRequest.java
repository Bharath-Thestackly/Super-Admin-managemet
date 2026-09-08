package com.enterprise.superadmin.platform_branding_service.dto.request;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BrandingUpdateRequest {

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brandName;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Pattern(
            regexp = "^#[A-Fa-f0-9]{6}$",
            message = "Primary color must be a valid HEX color"
    )
    private String primaryColor;

    @Pattern(
            regexp = "^#[A-Fa-f0-9]{6}$",
            message = "Secondary color must be a valid HEX color"
    )
    private String secondaryColor;

    @Size(max = 500, message = "Favicon URL must not exceed 500 characters")
    private String faviconUrl;

    public BrandingUpdateRequest() {
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }
}