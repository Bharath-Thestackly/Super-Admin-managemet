package com.example.microservice.organisations.branches.service;

import com.example.microservice.organisations.branches.dto.BranchRequestDTO;
import com.example.microservice.organisations.branches.dto.BranchResponseDTO;
import com.example.microservice.organisations.branches.entity.BranchEntity;
import com.example.microservice.organisations.branches.repository.BranchRepository;
import com.example.microservice.common.abstracts.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class BranchService extends AbstractService<
        BranchEntity,
        Long,
        BranchRequestDTO,
        BranchResponseDTO> {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        super(branchRepository, "Branch");
        this.branchRepository = branchRepository;
    }

    @Override
    protected BranchEntity toEntity(BranchRequestDTO dto) {
        BranchEntity entity = new BranchEntity();

        entity.setBranchCode(dto.getBranchCode());
        entity.setBranchName(dto.getBranchName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    @Override
    protected BranchResponseDTO toDto(BranchEntity entity) {
        BranchResponseDTO dto = new BranchResponseDTO();

        dto.setId(entity.getId());
        dto.setBranchCode(entity.getBranchCode());
        dto.setBranchName(entity.getBranchName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());

        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setVersion(entity.getVersion());

        return dto;
    }

    @Override
    protected void updateEntityFromDto(
            BranchEntity entity,
            BranchRequestDTO dto) {

        entity.setBranchCode(dto.getBranchCode());
        entity.setBranchName(dto.getBranchName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
    }

    @Override
    protected void beforeCreate(
            BranchEntity entity,
            BranchRequestDTO dto) {

        if (branchRepository.existsByBranchCode(dto.getBranchCode())) {
            throw new IllegalArgumentException(
                    "Branch code already exists: " + dto.getBranchCode()
            );
        }
    }

    @Override
    protected void beforeUpdate(
            BranchEntity entity,
            BranchRequestDTO dto) {

        branchRepository.findByBranchCode(dto.getBranchCode())
                .ifPresent(existingBranch -> {
                    if (!existingBranch.getId().equals(entity.getId())) {
                        throw new IllegalArgumentException(
                                "Branch code already exists: "
                                        + dto.getBranchCode()
                        );
                    }
                });
    }
}