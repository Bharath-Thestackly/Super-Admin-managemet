package com.example.microservice.organizations.department.service;



import java.util.List;

import com.example.microservice.organizations.department.dto.DepartmentResponse;



public interface DepartmentService {

    List<DepartmentResponse> searchDepartments(String departmentName);

}