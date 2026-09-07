package com.example.microservice.organizations.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import com.example.microservice.common.abstracts.BaseEntity;

/** Maps the confirmed organizations table; email is the documented Teams API extension. */
@Entity
@Table(name = "organizations", schema = "public",
        uniqueConstraints = @UniqueConstraint(name = "organizations_code_key", columnNames = "code"))
public class Company extends BaseEntity {
    @Column(name = "organization_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "code", nullable = false, length = 50)
    private String companyCode;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "industry", nullable = false, length = 100)
    private String industry;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDate deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    public Company() { }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDate getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDate deletedAt) { this.deletedAt = deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
}


