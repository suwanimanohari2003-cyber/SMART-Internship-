package com.example.internship.service;

import com.example.internship.model.Application;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.repository.ApplicationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Member 7 — ApplicationService
 * Beyond-CRUD: Audit Trail — status changes are timestamped via BaseEntity.updatedAt
 *
 * Member 10 — @CacheEvict on every write so /analytics/pass-rate stays fresh:
 *   - applyForJob()        → new application changes pass-rate denominator
 *   - updateStatus()       → status change affects ACCEPTED count (numerator)
 *   - withdrawApplication() → removes an application, affects both
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    public ApplicationService(ApplicationRepository applicationRepository,
                               EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
    }

    /**
     * Apply for a job.
     * Prevents duplicate applications and applications to expired/closed postings.
     * Evicts the passRate cache because total application count changes.
     */
    @Caching(evict = {
        @CacheEvict(value = "passRate",  allEntries = true),
        @CacheEvict(value = "trends",    allEntries = true)
    })
    public void applyForJob(Student student, JobPosting jobPosting) {
        // Duplicate check — returns 409 Conflict in controller
        if (applicationRepository.existsByStudentIdAndJobPostingId(
                student.getId(), jobPosting.getId())) {
            throw new RuntimeException("DUPLICATE: You have already applied for this job.");
        }
        // Deadline / status check — returns 400 Bad Request in controller
        if (jobPosting.getDeadline().isBefore(LocalDate.now())
                || "CLOSED".equals(jobPosting.getStatus())) {
            throw new RuntimeException("EXPIRED: This job posting is closed or the deadline has passed.");
        }

        Application app = new Application();
        app.setStudent(student);
        app.setJobPosting(jobPosting);
        app.setStatus("PENDING");
        app.setStatusChangedBy(student.getUser().getEmail());

        applicationRepository.save(app);

        // M9: async email confirmation
        emailService.sendApplicationConfirmation(student, jobPosting);
    }

    /**
     * Update application status (PENDING → SHORTLISTED → ACCEPTED / REJECTED).
     * Records who changed the status and when (audit trail via BaseEntity.updatedAt).
     * Evicts passRate cache because ACCEPTED count (numerator) may change.
     */
    @Caching(evict = {
        @CacheEvict(value = "passRate", allEntries = true)
    })
    public void updateStatus(Long applicationId, String newStatus, String changedByEmail) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        app.setStatus(newStatus);
        app.setStatusChangedBy(changedByEmail);   // audit trail
        applicationRepository.save(app);

        // M9: async email on acceptance or rejection
        emailService.sendStatusChangeAlert(app.getStudent(), app.getJobPosting(), newStatus);
    }

    /**
     * Withdraw a PENDING application.
     * Only allowed while status is PENDING — returns 400 otherwise.
     * Evicts passRate cache because application count changes.
     */
    @Caching(evict = {
        @CacheEvict(value = "passRate", allEntries = true),
        @CacheEvict(value = "trends",   allEntries = true)
    })
    public void withdrawApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        if (!"PENDING".equals(app.getStatus())) {
            throw new RuntimeException("INVALID: You can only withdraw PENDING applications.");
        }
        applicationRepository.delete(app);
    }
}
