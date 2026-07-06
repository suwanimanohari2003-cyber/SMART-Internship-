package com.example.internship.controller;

import com.example.internship.dto.CvRankResult;
import com.example.internship.model.Application;
import com.example.internship.model.Company;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.repository.ApplicationRepository;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.service.CvFilterService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company/jobs")
public class JobController {

    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CvFilterService cvFilterService;
    private final ApplicationRepository applicationRepository;

    public JobController(JobPostingRepository jobPostingRepository, CompanyRepository companyRepository,
                         UserRepository userRepository, StudentRepository studentRepository,
                         CvFilterService cvFilterService, ApplicationRepository applicationRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.cvFilterService = cvFilterService;
        this.applicationRepository = applicationRepository;
    }

    private Company currentCompany(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return companyRepository.findByUserId(userId).orElseThrow();
    }

    private void assertOwnership(JobPosting job, Company company) {
        if (job.getCompany() == null || !job.getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("This job posting does not belong to your company.");
        }
    }

    @GetMapping("/post")
    public String showPostJobForm(Authentication authentication, Model model) {
        model.addAttribute("job", new JobPosting());
        return "company/post-job";
    }

    @PostMapping("/post")
    public String saveJob(@ModelAttribute JobPosting job, Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        job.setCompany(company);
        if (job.getStatus() == null || job.getStatus().isBlank()) {
            job.setStatus("OPEN");
        }
        jobPostingRepository.save(job);
        model.addAttribute("msg", "Internship Job posted successfully!");
        model.addAttribute("job", new JobPosting());
        return "company/post-job";
    }

    @GetMapping("/manage")
    public String manageJobs(Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        List<JobPosting> jobs = jobPostingRepository.findByCompanyId(company.getId());
        model.addAttribute("jobs", jobs);
        model.addAttribute("companyName", company.getName());
        return "company/manage-jobs";
    }

    @PostMapping("/{id}/close")
    public String closeJob(@PathVariable Long id, Authentication authentication) {
        Company company = currentCompany(authentication);
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();
        assertOwnership(job, company);
        job.setStatus("CLOSED");
        jobPostingRepository.save(job);
        return "redirect:/company/jobs/manage?msg=Job successfully closed.";
    }

    // --- MEMBER 6: CV RANKING ENDPOINT — ranked, real applicants for one job ---
    @GetMapping("/{id}/ranked-applicants")
    public String showRankedApplicants(@PathVariable Long id, Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();
        assertOwnership(job, company);

        List<Application> applications = applicationRepository.findByJobPostingId(id);

        // Build a quick lookup from studentId -> CvRankResult so the template
        // can show each applicant's match% alongside their application/status.
        List<Student> applicantStudents = applications.stream()
            .map(Application::getStudent)
            .collect(Collectors.toList());
        List<CvRankResult> rankedResults = cvFilterService.rankApplicants(job, applicantStudents);

        model.addAttribute("companyName", company.getName());
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("rankedResults", rankedResults);
        model.addAttribute("selectedJobId", id);
        model.addAttribute("totalApplicants", applications.size());
        model.addAttribute("countPending", applications.stream().filter(a -> "PENDING".equals(a.getStatus())).count());
        model.addAttribute("countShortlisted", applications.stream().filter(a -> "SHORTLISTED".equals(a.getStatus())).count());
        model.addAttribute("countAccepted", applications.stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count());
        model.addAttribute("countRejected", applications.stream().filter(a -> "REJECTED".equals(a.getStatus())).count());

        List<JobPosting> jobs = jobPostingRepository.findByCompanyId(company.getId());
        model.addAttribute("jobs", jobs);

        return "company/applicants";
    }
}
