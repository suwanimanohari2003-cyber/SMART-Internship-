package com.example.internship.repository;

import com.example.internship.model.Company;
import com.example.internship.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

    List<JobPosting> findByCompanyId(Long companyId);

    List<JobPosting> findByCompany(Company company);

    List<JobPosting> findByDeadlineBeforeAndStatus(LocalDate date, String status);

    long countByCompanyAndStatus(Company company, String status);

    long countByStatus(String status);

    List<JobPosting> findByStatus(String status);
}
