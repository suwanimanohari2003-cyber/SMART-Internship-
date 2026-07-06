package com.example.internship.service;

import com.example.internship.model.Application;
import com.example.internship.model.InterviewSession;
import com.example.internship.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Member 9 — AnalyticsService
 * Beyond-CRUD: analytics and metrics.
 *
 * Member 10 — all hot endpoints are @Cacheable (TTL 10 min via CacheConfig).
 */
@Service
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewSessionRepository sessionRepository;

    public AnalyticsService(StudentRepository studentRepository,
                            CompanyRepository companyRepository,
                            JobPostingRepository jobPostingRepository,
                            ApplicationRepository applicationRepository,
                            InterviewSessionRepository sessionRepository) {
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.applicationRepository = applicationRepository;
        this.sessionRepository = sessionRepository;
    }

    /** Global pass rate across all interview sessions. */
    @Cacheable("passRate")
    public double getPassRate() {
        List<InterviewSession> all = sessionRepository.findAll();
        if (all.isEmpty()) return 0.0;
        long passed = all.stream().filter(InterviewSession::isPassed).count();
        return Math.round(((double) passed / all.size()) * 1000.0) / 10.0;
    }

    /**
     * Issue 8 fix: per-job pass rate.
     * Filters sessions to those for the given jobPostingId.
     */
    @Cacheable(value = "passRate", key = "#jobId")
    public double getPassRateByJob(Long jobId) {
        List<InterviewSession> sessions = sessionRepository.findByJobPostingId(jobId);
        if (sessions.isEmpty()) return 0.0;
        long passed = sessions.stream().filter(InterviewSession::isPassed).count();
        return Math.round(((double) passed / sessions.size()) * 1000.0) / 10.0;
    }

    /** Top N skills across all open job postings, sorted by frequency. */
    @Cacheable("topSkills")
    public Map<String, Integer> getTopSkillsInDemand(int limit) {
        Map<String, Integer> skillCount = new HashMap<>();
        jobPostingRepository.findAll().forEach(jp -> {
            if (jp.getRequiredSkills() == null) return;
            for (String skill : jp.getRequiredSkills().split(",")) {
                String s = skill.trim().toLowerCase();
                if (!s.isEmpty()) skillCount.merge(s, 1, Integer::sum);
            }
        });
        return skillCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

    /** Application counts grouped by month for the last N months. */
    @Cacheable("trends")
    public Map<String, Integer> getApplicationTrendByMonth(int months) {
        Map<String, Integer> trend = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusMonths(months);

        applicationRepository.findAll().stream()
            .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(cutoff))
            .forEach(a -> {
                String key = a.getCreatedAt().format(fmt);
                trend.merge(key, 1, Integer::sum);
            });
        return trend;
    }

    /** All key metrics in a single call — used by admin dashboard. */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalStudents",      studentRepository.count());
        m.put("totalCompanies",     companyRepository.count());
        m.put("totalJobs",          jobPostingRepository.count());
        m.put("activeJobs",         jobPostingRepository.countByStatus("OPEN"));
        m.put("totalApplications",  applicationRepository.count());
        m.put("passRate",           getPassRate());
        return m;
    }
}
