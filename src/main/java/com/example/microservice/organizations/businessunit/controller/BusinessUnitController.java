package com.example.microservice.organizations.businessunit.controller;

import com.example.microservice.common.abstracts.AbstractController;
import com.example.microservice.common.response.ApiResponse;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.microservice.organizations.businessunit.entity.BusinessUnit;
import com.example.microservice.organizations.businessunit.service.BusinessUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Business Unit Management.
 * Extends the platform's AbstractController with Long ID, automatically inheriting standard enterprise
 * CRUD endpoints (POST, GET /{id}, GET (paged), GET /all, PUT /{id}, DELETE /{id}).
 */
@RestController
@RequestMapping("/business-units") // Context-path is /api in application.yml -> full path is /api/business-units
public class BusinessUnitController extends AbstractController<BusinessUnit, Long, BusinessUnitRequestDto, BusinessUnitResponseDto> {

    public BusinessUnitController(BusinessUnitService service) {
        super(service);
    }

    /**
     * Custom endpoint beyond standard CRUD:
     * GET /api/business-units/search?query={keyword}
     * Searches business units across unitCode and unitName (case-insensitive).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BusinessUnitResponseDto>>> searchBusinessUnits(
            @RequestParam("query") String query) {
        List<BusinessUnitResponseDto> results = ((BusinessUnitService) service).searchBusinessUnits(query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
