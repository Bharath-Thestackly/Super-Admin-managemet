package com.example.microservice.organizations.department.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.microservice.common.abstracts.AbstractController;
import com.example.microservice.organizations.department.dto.DepartmentRequest;
import com.example.microservice.organizations.department.dto.DepartmentResponse;
import com.example.microservice.organizations.department.entity.Department;
import com.example.microservice.organizations.department.service.DepartmentServiceImpl;

@RestController
@RequestMapping("/departments")
public class DepartmentController extends AbstractController<
        Department,
        Long,
        DepartmentRequest,
        DepartmentResponse> {

    private final DepartmentServiceImpl departmentService;

    public DepartmentController(DepartmentServiceImpl departmentService) {
        super(departmentService);
        this.departmentService = departmentService;
    }

    @GetMapping("/search")
    public List<DepartmentResponse> searchDepartments(
            @RequestParam String departmentName) {

        return departmentService.searchDepartments(departmentName);
    }
}