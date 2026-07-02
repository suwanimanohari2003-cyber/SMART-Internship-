package com.example.internship.repository;

import com.example.internship.model.Application;
import com.example.internship.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findByStudentId(Long studentId, Pageable pageable);

    Page<Application> findByJobPostingId(Long jobPostingId, Pageable pageable);

    List<Application> findByJobPostingId(Long jobPostingId);

    boolean existsByStudentIdAndJobPostingId(Long studentId, Long jobPostingId);

    Optional<Application> findByStudentIdAndJobPostingId(Long studentId, Long jobPostingId);

    List<Application> findByStudent(Student student);

    // ── Company-scoped queries (Reports / Applicants / Dashboard) ──────────────
    List<Application> findByJobPosting_Company_Id(Long companyId);

    List<Application> findByJobPosting_Company_IdAndStatus(Long companyId, String status);

    long countByJobPosting_Company_Id(Long companyId);

    long countByJobPosting_Company_IdAndStatus(Long companyId, String status);

    long countByJobPostingIdAndStatus(Long jobPostingId, String status);
}
