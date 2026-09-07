package com.example.microservice.organizations.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

@Schema(description = "Company create/update payload. Strings are trimmed; omitted email is cleared on update.")
public record CompanyRequestDto(
        @NotBlank @Size(max = 50)
        @Schema(example = "ACME", description = "Globally unique code normalized to uppercase") String companyCode,
        @NotBlank @Size(max = 255) @Schema(example = "Acme Limited") String companyName,
        @Email @Size(max = 255) @Schema(example = "contact@acme.example") String email,
        @NotBlank @Size(max = 100) @Schema(example = "Manufacturing") String industry,
        @NotBlank @Size(max = 100) @Schema(example = "India") String country,
        @NotBlank @Size(max = 50) @Schema(example = "Active") String status
) {
    public CompanyRequestDto {
        companyCode = clean(companyCode);
        companyCode = companyCode == null ? null : companyCode.toUpperCase(Locale.ROOT);
        companyName = clean(companyName);
        email = clean(email);
        industry = clean(industry);
        country = clean(country);
        status = clean(status);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.strip();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
