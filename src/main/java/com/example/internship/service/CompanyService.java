package com.example.internship.service;

import com.example.internship.model.Company;
import com.example.internship.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Member 5 — CompanyService
 * Handles company profile registration, updates, and retrieval.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company registerCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateProfile(Long id, Company updatedData) {
        Company existing = companyRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Company not found with id: " + id)
        );
        existing.setName(updatedData.getName());
        existing.setIndustry(updatedData.getIndustry());
        existing.setLocation(updatedData.getLocation());
        existing.setWebsite(updatedData.getWebsite());
        existing.setDescription(updatedData.getDescription());
        existing.setProfileComplete(true);
        return companyRepository.save(existing);
    }

    public Optional<Company> getById(Long id) {
        return companyRepository.findById(id);
    }
}
