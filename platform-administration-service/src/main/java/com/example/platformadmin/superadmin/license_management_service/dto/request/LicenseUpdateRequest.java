package com.example.platformadmin.superadmin.license_management_service.dto.request;


import com.example.platformadmin.superadmin.license_management_service.enums.LicenseType;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record LicenseUpdateRequest(

        @NotBlank(message = "License plan is required")
        String licensePlan,

        LicenseType licenseType,

        LocalDate activationDate,

        LocalDate expiryDate
) {
}