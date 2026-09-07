package com.example.microservice.organizations.businessunit.service;

import com.example.microservice.common.abstracts.BaseService;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.microservice.organizations.businessunit.entity.BusinessUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Business Unit Service extending the platform's generic BaseService with Long ID.
 * Inherits standard enterprise CRUD operations: create, getById, getAll, update, deleteById, existsById.
 */
public interface BusinessUnitService extends BaseService<BusinessUnit, Long, BusinessUnitRequestDto, BusinessUnitResponseDto> {

    // Custom domain operation beyond standard CRUD
    List<BusinessUnitResponseDto> searchBusinessUnits(String query);

    // Convenience aliases for domain-specific naming
    default BusinessUnitResponseDto createBusinessUnit(BusinessUnitRequestDto request) {
        return create(request);
    }

    default BusinessUnitResponseDto getBusinessUnitById(Long id) {
        return getById(id);
    }

    default Page<BusinessUnitResponseDto> listBusinessUnitsPaged(Pageable pageable) {
        return getAll(pageable);
    }

    default List<BusinessUnitResponseDto> listAllBusinessUnits() {
        return getAll();
    }

    default BusinessUnitResponseDto updateBusinessUnit(Long id, BusinessUnitRequestDto request) {
        return update(id, request);
    }

    default void deleteBusinessUnit(Long id) {
        deleteById(id);
    }
}
