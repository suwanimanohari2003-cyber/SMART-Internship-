package com.example.internship.repository;

import com.example.internship.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByStudentId(Long studentId);
    List<InterviewSession> findByJobPostingId(Long jobPostingId);
    List<InterviewSession> findByJobPosting_Company_Id(Long companyId);
    long countByJobPosting_Company_Id(Long companyId);
    java.util.Optional<InterviewSession> findFirstByStudentIdAndJobPostingId(Long studentId, Long jobPostingId);
}
