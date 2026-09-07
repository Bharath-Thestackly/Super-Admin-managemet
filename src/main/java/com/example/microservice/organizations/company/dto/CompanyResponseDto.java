package com.example.microservice.organizations.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;


@Schema(description = "Stored company fields; audit timestamps are inherited from BaseEntity")
public record CompanyResponseDto(
        Long id,
        String companyCode,
        String companyName,
        String email,
        String industry,
        String country,
        String status,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) { }

