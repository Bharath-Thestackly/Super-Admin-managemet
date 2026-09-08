package com.example.microservice.organizations.businessunit.controller;

import com.example.microservice.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.microservice.organizations.businessunit.service.BusinessUnitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BusinessUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessUnitService service;

    private Long sampleId;
    private UUID  sampleOrgId;
    private BusinessUnitResponseDto sampleResponse;
    private BusinessUnitRequestDto sampleRequest;

    @BeforeEach
    void setUp() {
        sampleId = 1L;
        sampleOrgId = UUID.randomUUID();

        sampleResponse = BusinessUnitResponseDto.builder()
                .id(sampleId)
                .unitName("FinTech Unit")
                .unitCode("BU-FIN-01")
                .description("Payment gateways")
                .organizationId(sampleOrgId)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .createdBy("ADMIN")
                .updatedAt(LocalDateTime.now())
                .updatedBy("ADMIN")
                .build();

        sampleRequest = new BusinessUnitRequestDto();
        sampleRequest.setUnitName("FinTech Unit");
        sampleRequest.setUnitCode("BU-FIN-01");
        sampleRequest.setDescription("Payment gateways");
        sampleRequest.setOrganizationId(sampleOrgId);
        sampleRequest.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("POST /api/business-units - Inherited from AbstractController")
    void testCreateEndpoint() throws Exception {
        when(service.create(any(BusinessUnitRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/business-units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unitCode").value("BU-FIN-01"))
                .andExpect(jsonPath("$.data.unitName").value("FinTech Unit"));
    }

    @Test
    @DisplayName("GET /api/business-units/{id} - Inherited from AbstractController")
    void testGetByIdEndpoint() throws Exception {
        when(service.getById(sampleId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/business-units/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(sampleId))
                .andExpect(jsonPath("$.data.unitCode").value("BU-FIN-01"));
    }

    @Test
    @DisplayName("GET /api/business-units/search - Custom endpoint in BusinessUnitController")
    void testSearchEndpoint() throws Exception {
        when(service.searchBusinessUnits("FIN")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/business-units/search")
                        .param("query", "FIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].unitCode").value("BU-FIN-01"));
    }

    @Test
    @DisplayName("PUT /api/business-units/{id} - Inherited from AbstractController")
    void testUpdateEndpoint() throws Exception {
        when(service.update(eq(sampleId), any(BusinessUnitRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/business-units/{id}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource updated successfully"));
    }

    @Test
    @DisplayName("DELETE /api/business-units/{id} - Inherited from AbstractController")
    void testDeleteEndpoint() throws Exception {
        mockMvc.perform(delete("/business-units/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource deleted successfully"));
    }
}
