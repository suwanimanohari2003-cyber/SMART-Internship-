package com.example.internship.model;

import jakarta.persistence.*;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "job_posting_id"})
})
public class Application extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "status_changed_by")
    private String statusChangedBy;

    // --- Getters and Setters ---

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting jobPosting) { this.jobPosting = jobPosting; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusChangedBy() { return statusChangedBy; }
    public void setStatusChangedBy(String statusChangedBy) { this.statusChangedBy = statusChangedBy; }

    /**
     * Convenience alias so templates can read application.appliedAt
     * (the schema column is applied_at, but BaseEntity maps the
     * actual persisted timestamp as createdAt). Keeping this getter
     * avoids having to touch every template that already uses appliedAt.
     */
    public java.time.LocalDateTime getAppliedAt() { return getCreatedAt(); }
}
