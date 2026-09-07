package com.example.microservice.organizations.company.repository;

import com.example.microservice.organizations.company.entity.Company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("""
            select company from Company company
            where company.deleted = false
              and (lower(company.companyCode) like :pattern escape '\\'
                or lower(company.companyName) like :pattern escape '\\'
                or lower(company.email) like :pattern escape '\\')
            order by company.companyName asc, company.id asc
    """)
    List<Company> searchCompanies(@Param("pattern") String pattern);

    boolean existsByCompanyCodeIgnoreCase(String companyCode);
    boolean existsByCompanyCodeIgnoreCaseAndIdNot(String companyCode, Long id);
}

