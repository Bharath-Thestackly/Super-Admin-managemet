package com.enterprise.superadmin.platform_branding_service.dto.response;

public class BrandingResponse {

    private Long id;

    private String brandName;

    private String logoUrl;

    private String primaryColor;

    private String secondaryColor;

    private String faviconUrl;

    private String status;

    public BrandingResponse() {
    }

    public BrandingResponse(
            Long id,
            String brandName,
            String logoUrl,
            String primaryColor,
            String secondaryColor,
            String faviconUrl,
            String status
    ) {
        this.id = id;
        this.brandName = brandName;
        this.logoUrl = logoUrl;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.faviconUrl = faviconUrl;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}