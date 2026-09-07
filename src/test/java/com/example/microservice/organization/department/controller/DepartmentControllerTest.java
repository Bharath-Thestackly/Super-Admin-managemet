package com.example.microservice.organization.department.controller;

import com.example.microservice.organizations.department.dto.DepartmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateDepartment() throws Exception {

        DepartmentRequest request = new DepartmentRequest();
        request.setDepartmentCode("DEP001");
        request.setDepartmentName("IT");
        request.setDescription("IT Department");
        request.setActive(true);

        mockMvc.perform(post("/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testGetDepartmentById() throws Exception {

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testGetAllDepartments() throws Exception {

        mockMvc.perform(get("/departments"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testUpdateDepartment() throws Exception {

        DepartmentRequest request = new DepartmentRequest();
        request.setDepartmentCode("DEP001");
        request.setDepartmentName("IT Updated");
        request.setDescription("Updated Department");
        request.setActive(true);

        mockMvc.perform(put("/departments/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDepartment() throws Exception {

        mockMvc.perform(delete("/departments/{id}", 1L))
                .andExpect(status().isNotFound());
    }
    @Test
    void testSearchDepartments() throws Exception {

        mockMvc.perform(get("/departments/search")
                        .param("departmentName", "IT"))
                .andExpect(status().is2xxSuccessful());
    }
}