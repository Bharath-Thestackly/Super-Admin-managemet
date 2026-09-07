package com.example.microservice.organization.department.service;



import java.util.List;

import com.example.microservice.organization.department.dto.DepartmentResponse;



public interface DepartmentService {

    List<DepartmentResponse> searchDepartments(String departmentName);

}