package com.example.microservice.organizations.department.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.microservice.common.abstracts.AbstractService;
import com.example.microservice.organizations.department.dto.DepartmentRequest;
import com.example.microservice.organizations.department.dto.DepartmentResponse;
import com.example.microservice.organizations.department.entity.Department;
import com.example.microservice.organizations.department.repository.DepartmentRepository;

@Service
public class DepartmentServiceImpl
        extends AbstractService<
                Department,
                Long,
                DepartmentRequest,
                DepartmentResponse>
        implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        super(departmentRepository, "Department");
        this.departmentRepository = departmentRepository;
    }

    @Override
    protected Department toEntity(DepartmentRequest dto) {

        Department department = new Department();

        department.setDepartmentCode(dto.getDepartmentCode());
        department.setDepartmentName(dto.getDepartmentName());
        department.setDescription(dto.getDescription());
        department.setActive(dto.getActive());

        return department;
    }

    @Override
    protected DepartmentResponse toDto(Department entity) {

        DepartmentResponse response = new DepartmentResponse();

        response.setId(entity.getId());
        response.setDepartmentCode(entity.getDepartmentCode());
        response.setDepartmentName(entity.getDepartmentName());
        response.setDescription(entity.getDescription());
        response.setActive(entity.getActive());

        return response;
    }

    @Override
    protected void updateEntityFromDto(
            Department entity,
            DepartmentRequest dto) {

        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDepartmentName(dto.getDepartmentName());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.getActive());
    }

    @Override
    public List<DepartmentResponse> searchDepartments(String departmentName) {

        return departmentRepository
                .findByDepartmentNameContainingIgnoreCase(departmentName)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}