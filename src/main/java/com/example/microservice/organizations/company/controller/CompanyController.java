package com.example.microservice.organizations.company.controller;

import com.example.microservice.common.abstracts.AbstractController;
import com.example.microservice.common.response.ApiResponse;
import com.example.microservice.organizations.company.dto.CompanyRequestDto;
import com.example.microservice.organizations.company.dto.CompanyResponseDto;
import com.example.microservice.organizations.company.entity.Company;
import com.example.microservice.organizations.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Standard CRUD routes are inherited from AbstractController.
 * Only Company-specific routes belong in this controller.
 */
@RestController
@RequestMapping("/companies")
public class CompanyController
        extends AbstractController<Company, Long, CompanyRequestDto, CompanyResponseDto> {

    public CompanyController(CompanyService service) {
        super(service);
    }

    /**
     * GET /api/companies/search?query={keyword}
     * Searches companyCode, companyName, and email without case sensitivity.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> searchCompanies(
            @RequestParam("query") String query) {
        List<CompanyResponseDto> results = ((CompanyService) service).searchCompanies(query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}

