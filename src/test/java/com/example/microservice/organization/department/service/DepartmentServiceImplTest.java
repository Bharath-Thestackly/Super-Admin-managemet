package com.example.microservice.organization.department.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.microservice.organizations.department.dto.DepartmentRequest;
import com.example.microservice.organizations.department.dto.DepartmentResponse;
import com.example.microservice.organizations.department.entity.Department;
import com.example.microservice.organizations.department.repository.DepartmentRepository;
import com.example.microservice.organizations.department.service.DepartmentServiceImpl;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void testSearchDepartments() {

        Department department = new Department();
        department.setId(1L);
        department.setDepartmentCode("DEP001");
        department.setDepartmentName("IT");
        department.setDescription("IT Department");
        department.setActive(true);

        when(departmentRepository
                .findByDepartmentNameContainingIgnoreCase("IT"))
                .thenReturn(List.of(department));

        List<DepartmentResponse> result =
                departmentService.searchDepartments("IT");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("IT", result.get(0).getDepartmentName());
        assertEquals("DEP001", result.get(0).getDepartmentCode());
        assertEquals(true, result.get(0).getActive());
    }
    @Test
    void testCreateDepartment() {

        DepartmentRequest request = new DepartmentRequest();
        request.setDepartmentCode("DEP001");
        request.setDepartmentName("IT");
        request.setDescription("IT Department");
        request.setActive(true);

        Department department = new Department();
        department.setId(1L);
        department.setDepartmentCode("DEP001");
        department.setDepartmentName("IT");
        department.setDescription("IT Department");
        department.setActive(true);

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);

        DepartmentResponse response = departmentService.create(request);

        assertNotNull(response);
        assertEquals("DEP001", response.getDepartmentCode());
        assertEquals("IT", response.getDepartmentName());

        verify(departmentRepository, times(1))
                .save(any(Department.class));
    }
    @Test
    void testGetById() {

        Department department = new Department();
        department.setId(1L);
        department.setDepartmentCode("DEP001");
        department.setDepartmentName("IT");
        department.setDescription("IT Department");
        department.setActive(true);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        DepartmentResponse response =
                departmentService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("IT", response.getDepartmentName());
    }
    @Test
    void testGetAllDepartments() {

        Department department = new Department();
        department.setId(1L);
        department.setDepartmentCode("DEP001");
        department.setDepartmentName("IT");

        when(departmentRepository.findAll())
                .thenReturn(List.of(department));

        List<DepartmentResponse> result =
                departmentService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("IT", result.get(0).getDepartmentName());
    }
    @Test
    void testUpdateDepartment() {

        Department department = new Department();
        department.setId(1L);
        department.setDepartmentCode("DEP001");
        department.setDepartmentName("IT");

        DepartmentRequest request = new DepartmentRequest();
        request.setDepartmentCode("DEP001");
        request.setDepartmentName("IT UPDATED");
        request.setDescription("Updated Department");
        request.setActive(true);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(department);

        DepartmentResponse response =
                departmentService.update(1L, request);

        assertNotNull(response);

        verify(departmentRepository, times(1))
                .save(any(Department.class));
    }
    @Test
    void testDeleteDepartment() {

        when(departmentRepository.existsById(1L))
                .thenReturn(true);

        departmentService.deleteById(1L);

        verify(departmentRepository, times(1))
                .deleteById(1L);
    }
}