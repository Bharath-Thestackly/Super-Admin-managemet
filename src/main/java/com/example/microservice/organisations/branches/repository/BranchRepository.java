package com.example.microservice.organisations.branches.repository;

import com.example.microservice.organisations.branches.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<BranchEntity, Long> {

    Optional<BranchEntity> findByBranchCode(String branchCode);

    boolean existsByBranchCode(String branchCode);
}