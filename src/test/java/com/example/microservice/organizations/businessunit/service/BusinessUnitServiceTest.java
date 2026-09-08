package com.example.microservice.organizations.businessunit.service;

import com.example.microservice.common.exception.BadRequestException;
import com.example.microservice.common.exception.ResourceNotFoundException;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.microservice.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.microservice.organizations.businessunit.entity.BusinessUnit;
import com.example.microservice.organizations.businessunit.repository.BusinessUnitRepository;
import com.example.microservice.organizations.businessunit.service.impl.BusinessUnitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessUnitServiceTest {

    @Mock
    private BusinessUnitRepository repository;

    @InjectMocks
    private BusinessUnitServiceImpl service;

    private Long sampleId;
    private UUID sampleOrgId;
    private BusinessUnit sampleEntity;
    private BusinessUnitRequestDto sampleRequest;

    @BeforeEach
    void setUp() {
        sampleId = 1L;
        sampleOrgId = UUID.randomUUID();

        sampleEntity = BusinessUnit.builder()
                .id(sampleId)
                .unitName("Cloud Solutions")
                .unitCode("BU-CLOUD-01")
                .description("Cloud Architecture and DevOps")
                .organizationId(sampleOrgId)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .createdBy("ADMIN")
                .updatedAt(LocalDateTime.now())
                .updatedBy("ADMIN")
                .tenantId("default")
                .isDeleted(false)
                .build();

        sampleRequest = new BusinessUnitRequestDto();
        sampleRequest.setUnitName("Cloud Solutions");
        sampleRequest.setUnitCode("BU-CLOUD-01");
        sampleRequest.setDescription("Cloud Architecture and DevOps");
        sampleRequest.setOrganizationId(sampleOrgId);
        sampleRequest.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("BaseService.create: Success")
    void testCreate_Success() {
        when(repository.existsByUnitCodeAndIsDeletedFalse("BU-CLOUD-01")).thenReturn(false);
        when(repository.save(any(BusinessUnit.class))).thenReturn(sampleEntity);

        BusinessUnitResponseDto response = service.create(sampleRequest);

        assertNotNull(response);
        assertEquals("Cloud Solutions", response.getUnitName());
        assertEquals("BU-CLOUD-01", response.getUnitCode());
        assertEquals(sampleOrgId, response.getOrganizationId());
        verify(repository, times(1)).save(any(BusinessUnit.class));
    }

    @Test
    @DisplayName("BaseService.create: Duplicate Code Throws BadRequestException")
    void testCreate_DuplicateCode_ThrowsException() {
        when(repository.existsByUnitCodeAndIsDeletedFalse("BU-CLOUD-01")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            service.create(sampleRequest);
        });

        verify(repository, never()).save(any(BusinessUnit.class));
    }

    @Test
    @DisplayName("BaseService.getById: Success")
    void testGetById_Success() {
        when(repository.findByIdAndIsDeletedFalse(sampleId)).thenReturn(Optional.of(sampleEntity));

        BusinessUnitResponseDto response = service.getById(sampleId);

        assertNotNull(response);
        assertEquals(sampleId, response.getId());
        assertEquals("Cloud Solutions", response.getUnitName());
    }

    @Test
    @DisplayName("BaseService.getById: Throws 404 when ID does not exist")
    void testGetById_NotFound() {
        when(repository.findByIdAndIsDeletedFalse(sampleId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getById(sampleId);
        });
    }

    @Test
    @DisplayName("BaseService.getAll (Paged): Returns paginated records")
    void testGetAllPaged() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findByIsDeletedFalse(pageRequest)).thenReturn(new PageImpl<>(List.of(sampleEntity)));

        Page<BusinessUnitResponseDto> page = service.getAll(pageRequest);

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("BU-CLOUD-01", page.getContent().get(0).getUnitCode());
    }

    @Test
    @DisplayName("BaseService.getAll (Unpaged): Returns all records")
    void testGetAllUnpaged() {
        when(repository.findByIsDeletedFalse()).thenReturn(List.of(sampleEntity));

        List<BusinessUnitResponseDto> list = service.getAll();

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("BaseService.update: Successfully modifies fields")
    void testUpdate_Success() {
        when(repository.findByIdAndIsDeletedFalse(sampleId)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(BusinessUnit.class))).thenReturn(sampleEntity);

        sampleRequest.setUnitName("Cloud Solutions Updated");

        BusinessUnitResponseDto updated = service.update(sampleId, sampleRequest);

        assertNotNull(updated);
        verify(repository, times(1)).save(sampleEntity);
    }

    @Test
    @DisplayName("BaseService.deleteById: Verifies Soft Delete flag is set to true")
    void testDeleteById_SoftDelete() {
        when(repository.findByIdAndIsDeletedFalse(sampleId)).thenReturn(Optional.of(sampleEntity));

        service.deleteById(sampleId);

        assertTrue(sampleEntity.getIsDeleted());
        assertNotNull(sampleEntity.getDeletedAt());
        verify(repository, times(1)).save(sampleEntity);
    }

    @Test
    @DisplayName("Custom search: Returns matching items")
    void testSearchBusinessUnits() {
        when(repository.searchBusinessUnits("CLOUD")).thenReturn(List.of(sampleEntity));

        List<BusinessUnitResponseDto> results = service.searchBusinessUnits("CLOUD");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("BU-CLOUD-01", results.get(0).getUnitCode());
        verify(repository, times(1)).searchBusinessUnits("CLOUD");
    }
}
