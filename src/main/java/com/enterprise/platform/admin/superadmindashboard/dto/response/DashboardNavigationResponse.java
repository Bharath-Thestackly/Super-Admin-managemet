package com.enterprise.platform.admin.superadmindashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Quick navigation and quick-actions metadata for Super Admin UI")
public record DashboardNavigationResponse(
        List<NavigationItem> items,
        List<QuickAction> quickActions) {

    public record NavigationItem(String title, String path, String icon, String requiredRole) {}

    public record QuickAction(String id, String label, String actionUrl, String method) {}
}
