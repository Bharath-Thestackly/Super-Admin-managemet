package com.example.microservice.organisations.branches.controller;

import com.example.microservice.organisations.branches.entity.BranchEntity;
import com.example.microservice.organisations.branches.dto.BranchRequestDTO;
import com.example.microservice.organisations.branches.dto.BranchResponseDTO;
import com.example.microservice.organisations.branches.service.BranchService;
import com.example.microservice.common.abstracts.AbstractController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/branches")
@SecurityRequirement(name = "bearerAuth")
public class BranchController extends AbstractController<
        BranchEntity,
        Long,
        BranchRequestDTO,
        BranchResponseDTO> {

    public BranchController(BranchService branchService) {
        super(branchService);
    }
}