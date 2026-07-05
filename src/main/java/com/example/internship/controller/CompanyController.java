package com.example.internship.controller;

import com.example.internship.model.Application;
import com.example.internship.model.Company;
import com.example.internship.model.InterviewSession;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.model.User;
import com.example.internship.repository.ApplicationRepository;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.InterviewSessionRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.service.CvFilterService;
import com.example.internship.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final StudentRepository studentRepository;
    private final CvFilterService cvFilterService;
    private final FileStorageService fileStorageService;

    public CompanyController(CompanyRepository companyRepository,
                             JobPostingRepository jobPostingRepository,
                             UserRepository userRepository,
                             ApplicationRepository applicationRepository,
                             InterviewSessionRepository interviewSessionRepository,
                             StudentRepository studentRepository,
                             CvFilterService cvFilterService,
                             FileStorageService fileStorageService) {
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.studentRepository = studentRepository;
        this.cvFilterService = cvFilterService;
        this.fileStorageService = fileStorageService;
    }

    private Company currentCompany(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return companyRepository.findByUser(user).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        model.addAttribute("companyName", company.getName());

        List<JobPosting> jobs = jobPostingRepository.findByCompany(company);

        long activeJobs = jobs.stream().filter(j -> "OPEN".equals(j.getStatus())).count();
        model.addAttribute("activeJobs", activeJobs);

        long totalApplicants = applicationRepository.countByJobPosting_Company_Id(company.getId());
        model.addAttribute("totalApplicants", totalApplicants);

        long shortlisted = applicationRepository.countByJobPosting_Company_IdAndStatus(company.getId(), "SHORTLISTED");
        model.addAttribute("shortlisted", shortlisted);

        long interviews = interviewSessionRepository.countByJobPosting_Company_Id(company.getId());
        model.addAttribute("interviews", interviews);

        // Active job posts with real applicant counts, sorted by most recently created
        List<JobPosting> activeJobPosts = jobs.stream()
            .filter(j -> "OPEN".equals(j.getStatus()))
            .sorted(Comparator.comparing(JobPosting::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .collect(Collectors.toList());
        model.addAttribute("activeJobPosts", activeJobPosts);

        java.util.Map<Long, Long> applicantCountByJob = new java.util.HashMap<>();
        for (JobPosting j : activeJobPosts) {
            applicantCountByJob.put(j.getId(), (long) applicationRepository.findByJobPostingId(j.getId()).size());
        }
        model.addAttribute("applicantCountByJob", applicantCountByJob);
        model.addAttribute("totalOpenJobs", activeJobs);

        // Monthly applications trend (last 6 months) for the dashboard chart
        List<Application> companyApplications = applicationRepository.findByJobPosting_Company_Id(company.getId());
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM");
        java.util.LinkedHashMap<String, Integer> monthlyTrend = new java.util.LinkedHashMap<>();
        java.time.YearMonth now = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            monthlyTrend.put(now.minusMonths(i).format(monthFmt), 0);
        }
        for (Application a : companyApplications) {
            if (a.getCreatedAt() == null) continue;
            String key = java.time.YearMonth.from(a.getCreatedAt()).format(monthFmt);
            if (monthlyTrend.containsKey(key)) {
                monthlyTrend.merge(key, 1, Integer::sum);
            }
        }
        model.addAttribute("monthlyTrendLabels", monthlyTrend.keySet());
        model.addAttribute("monthlyTrendValues", monthlyTrend.values());

        return "company/dashboard";
    }

    /**
     * Applicants page — shows every application across all of the company's
     * job postings (real data: student name, GPA, university, status, CV link).
     * Supports optional jobId / status filtering via query params.
     */
    @GetMapping("/applicants")
    public String showApplicants(Authentication authentication, Model model,
                                  @RequestParam(required = false) Long jobId,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String q) {
        Company company = currentCompany(authentication);
        model.addAttribute("companyName", company.getName());

        List<JobPosting> jobs = jobPostingRepository.findByCompany(company);
        model.addAttribute("jobs", jobs);

        List<Application> applications = applicationRepository.findByJobPosting_Company_Id(company.getId());

        if (jobId != null) {
            applications = applications.stream()
                .filter(a -> a.getJobPosting().getId().equals(jobId))
                .collect(Collectors.toList());
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            applications = applications.stream()
                .filter(a -> a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        }
        if (q != null && !q.isBlank()) {
            String needle = q.trim().toLowerCase();
            applications = applications.stream()
                .filter(a -> (a.getStudent().getFullName() != null && a.getStudent().getFullName().toLowerCase().contains(needle))
                          || (a.getStudent().getUniversity() != null && a.getStudent().getUniversity().toLowerCase().contains(needle))
                          || (a.getJobPosting().getTitle() != null && a.getJobPosting().getTitle().toLowerCase().contains(needle)))
                .collect(Collectors.toList());
        }

        applications = applications.stream()
            .sorted(Comparator.comparing(Application::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        model.addAttribute("applications", applications);
        model.addAttribute("selectedJobId", jobId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("q", q);

        // Stat counts for the cards at the top of the page (computed from the
        // full, unfiltered application set so the cards always reflect totals)
        List<Application> allApps = applicationRepository.findByJobPosting_Company_Id(company.getId());
        model.addAttribute("countPending", allApps.stream().filter(a -> "PENDING".equals(a.getStatus())).count());
        model.addAttribute("countShortlisted", allApps.stream().filter(a -> "SHORTLISTED".equals(a.getStatus())).count());
        model.addAttribute("countAccepted", allApps.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count());
        model.addAttribute("countRejected", allApps.stream().filter(a -> "REJECTED".equals(a.getStatus())).count());
        model.addAttribute("totalApplicants", allApps.size());

        return "company/applicants";
    }

    /**
     * CV detail view for a single application: profile + skill match analysis
     * + CV download + accept/reject/shortlist actions, all wired to the real
     * ApplicationService so DB + email + student-side view stay in sync.
     */
    @GetMapping("/applicants/{applicationId}")
    public String viewApplicantCv(@PathVariable Long applicationId, Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        Application app = applicationRepository.findById(applicationId).orElseThrow();

        // Ownership guard: a company can only view applications for its own jobs
        if (!app.getJobPosting().getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your job posting.");
        }

        Student student = app.getStudent();
        model.addAttribute("companyName", company.getName());
        model.addAttribute("applicationId", applicationId);
        model.addAttribute("application", app);
        model.addAttribute("student", student);
        model.addAttribute("cvRankResult", cvFilterService.rankStudent(app.getJobPosting(), student));

        return "company/cv-detail";
    }

    /**
     * Streams the applicant's CV file to the company — view inline (browser PDF
     * viewer) or download. This is distinct from /student/download-cv, which
     * only ever serves the *currently logged-in* student's own CV and is
     * locked to the STUDENT role, so it cannot be reused by a company user.
     */
    @GetMapping("/applicants/{applicationId}/cv")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> viewApplicantCvFile(
            @PathVariable Long applicationId,
            @RequestParam(required = false, defaultValue = "inline") String mode,
            Authentication authentication) {
        Company company = currentCompany(authentication);
        Application app = applicationRepository.findById(applicationId).orElseThrow();

        if (!app.getJobPosting().getCompany().getId().equals(company.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your job posting.");
        }

        Student student = app.getStudent();
        if (student.getCvPath() == null || student.getCvPath().isBlank()) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        try {
            java.nio.file.Path filePath = fileStorageService.getFilePath(student.getCvPath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            String disposition = "attachment".equalsIgnoreCase(mode) ? "attachment" : "inline";
            String safeName = student.getFullName() != null
                ? student.getFullName().replaceAll("\\s+", "_") : "CV";
            return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                    disposition + "; filename=\"" + safeName + "_CV.pdf\"")
                .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Shortlisted-only view across all of the company's jobs (a focused
     * subset of the Applicants page) — Accept / Reject / View CV actions.
     */
    @GetMapping("/shortlisted")
    public String showShortlisted(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        model.addAttribute("companyName", company.getName());

        List<Application> applications = applicationRepository
            .findByJobPosting_Company_IdAndStatus(company.getId(), "SHORTLISTED")
            .stream()
            .sorted(Comparator.comparing(Application::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

        model.addAttribute("applications", applications);
        return "company/shortlist";
    }

    /**
     * Interview Questions landing page — questions are per job posting, so this
     * page lets the company pick which job to manage questions for, then links
     * into the existing CompanyInterviewController flow (/company/jobs/{id}/questions).
     */
    @GetMapping("/interview-questions")
    public String showInterviewQuestions(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        model.addAttribute("companyName", company.getName());

        List<JobPosting> jobs = jobPostingRepository.findByCompany(company);
        model.addAttribute("jobs", jobs);
        return "company/interview-questions";
    }

    /**
     * Reports & Analytics — real recruitment funnel + hiring trend for this
     * company only (computed from its own applications / interview sessions).
     */
    @GetMapping("/reports")
    public String showReports(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        model.addAttribute("companyName", company.getName());

        List<JobPosting> jobs = jobPostingRepository.findByCompany(company);
        List<Application> applications = applicationRepository.findByJobPosting_Company_Id(company.getId());
        List<InterviewSession> sessions = interviewSessionRepository.findByJobPosting_Company_Id(company.getId());

        long totalApplications = applications.size();
        long shortlisted = applications.stream().filter(a -> "SHORTLISTED".equals(a.getStatus())).count();
        long interviewed = sessions.stream().filter(s -> s.getCompletedAt() != null).count();
        long accepted = applications.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
        long rejected = applications.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
        long activeJobs = jobs.stream().filter(j -> "OPEN".equals(j.getStatus())).count();

        long passedInterviews = sessions.stream().filter(InterviewSession::isPassed).count();
        double passRate = sessions.isEmpty() ? 0.0
            : Math.round(((double) passedInterviews / sessions.size()) * 1000.0) / 10.0;

        double applicationsPerJob = jobs.isEmpty() ? 0.0
            : Math.round(((double) totalApplications / jobs.size()) * 10.0) / 10.0;

        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("shortlistedCount", shortlisted);
        model.addAttribute("interviewedCount", interviewed);
        model.addAttribute("acceptedCount", accepted);
        model.addAttribute("rejectedCount", rejected);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("passRate", passRate);
        model.addAttribute("applicationsPerJob", applicationsPerJob);

        double shortlistedPct = totalApplications == 0 ? 0.0 : round1(shortlisted * 100.0 / totalApplications);
        double interviewedPct = totalApplications == 0 ? 0.0 : round1(interviewed * 100.0 / totalApplications);
        double acceptedPct = totalApplications == 0 ? 0.0 : round1(accepted * 100.0 / totalApplications);
        model.addAttribute("shortlistedPct", shortlistedPct);
        model.addAttribute("interviewedPct", interviewedPct);
        model.addAttribute("acceptedPct", acceptedPct);

        // Hiring trend by month (last 6 months) — counts of applications created per month
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        java.util.LinkedHashMap<String, Integer> trend = new java.util.LinkedHashMap<>();
        java.time.YearMonth current = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            trend.put(current.minusMonths(i).format(fmt), 0);
        }
        for (Application a : applications) {
            if (a.getCreatedAt() == null) continue;
            String key = java.time.YearMonth.from(a.getCreatedAt()).format(fmt);
            if (trend.containsKey(key)) {
                trend.merge(key, 1, Integer::sum);
            }
        }
        model.addAttribute("trendLabels", trend.keySet());
        model.addAttribute("trendValues", trend.values());

        return "company/reports";
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /**
     * Company-scoped CSV export — only this company's own applications.
     * Lives under /company/** so the existing COMPANY role guard covers it
     * (the global /reports/** endpoints remain ADMIN-only by design).
     */
    @GetMapping("/reports/applications.csv")
    public void exportCompanyApplicationsCsv(Authentication authentication,
                                              jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Company company = currentCompany(authentication);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
            company.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_applications_report.csv\"");

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Student Name,Student Email,University,GPA,Job Title,Status,Applied At\n");

        applicationRepository.findByJobPosting_Company_Id(company.getId()).forEach(app -> {
            csv.append(app.getId()).append(",")
               .append(csvQuote(app.getStudent().getFullName())).append(",")
               .append(csvQuote(app.getStudent().getEmail())).append(",")
               .append(csvQuote(app.getStudent().getUniversity())).append(",")
               .append(app.getStudent().getGpa()).append(",")
               .append(csvQuote(app.getJobPosting().getTitle())).append(",")
               .append(app.getStatus()).append(",")
               .append(app.getCreatedAt() != null ? app.getCreatedAt().toString() : "N/A").append("\n");
        });

        try {
            response.getWriter().write(csv.toString());
        } catch (java.io.IOException e) {
            throw e;
        }
    }

    @GetMapping("/reports/applications.pdf")
    public void exportCompanyApplicationsPdf(Authentication authentication,
                                              jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Company company = currentCompany(authentication);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
            company.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_applications_report.pdf\"");

        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new com.lowagie.text.Paragraph(company.getName() + " — Applications Report"));
        document.add(new com.lowagie.text.Paragraph("Generated: " + java.time.LocalDateTime.now()));
        document.add(new com.lowagie.text.Paragraph("----------------------------------------------------"));

        List<Application> apps = applicationRepository.findByJobPosting_Company_Id(company.getId());
        document.add(new com.lowagie.text.Paragraph("Total Applications: " + apps.size()));
        document.add(new com.lowagie.text.Paragraph(" "));

        apps.forEach(app -> {
            try {
                document.add(new com.lowagie.text.Paragraph(
                    "• " + app.getStudent().getFullName() +
                    " → " + app.getJobPosting().getTitle() +
                    " [" + app.getStatus() + "]"
                ));
            } catch (Exception e) { /* skip malformed row */ }
        });

        document.close();
    }

    private String csvQuote(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    @GetMapping("/settings")
    public String showSettings(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        model.addAttribute("company", company);
        return "company/settings";
    }

    @PostMapping("/settings/update")
    public String updateProfile(@ModelAttribute Company updatedCompany, Authentication authentication) {
        Company existingCompany = currentCompany(authentication);

        existingCompany.setIndustry(updatedCompany.getIndustry());
        existingCompany.setWebsite(updatedCompany.getWebsite());
        existingCompany.setLocation(updatedCompany.getLocation());
        companyRepository.save(existingCompany);

        return "redirect:/company/settings?success";
    }
}