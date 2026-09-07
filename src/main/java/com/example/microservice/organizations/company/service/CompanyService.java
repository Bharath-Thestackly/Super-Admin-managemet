package com.example.microservice.organizations.company.service;

import com.example.microservice.common.abstracts.BaseService;
import com.example.microservice.organizations.company.dto.CompanyRequestDto;
import com.example.microservice.organizations.company.dto.CompanyResponseDto;
import com.example.microservice.organizations.company.entity.Company;

import java.util.List;


public interface CompanyService
        extends BaseService<Company, Long, CompanyRequestDto, CompanyResponseDto> {

    List<CompanyResponseDto> searchCompanies(String query);
}

