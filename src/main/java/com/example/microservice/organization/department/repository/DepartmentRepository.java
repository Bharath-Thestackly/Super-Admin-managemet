package com.example.microservice.organization.department.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.microservice.organization.department.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);

}