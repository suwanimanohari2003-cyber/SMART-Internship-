package com.example.internship.controller;

import com.example.internship.model.Application;
import com.example.internship.model.Company;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.model.User;
import com.example.internship.repository.ApplicationRepository;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.service.ApplicationService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    public ApplicationController(ApplicationService applicationService, JobPostingRepository jobPostingRepository,
                                 UserRepository userRepository, StudentRepository studentRepository,
                                 ApplicationRepository applicationRepository, CompanyRepository companyRepository) {
        this.applicationService = applicationService;
        this.jobPostingRepository = jobPostingRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.companyRepository = companyRepository;
    }

    private Company currentCompany(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return companyRepository.findByUser(user).orElseThrow();
    }

    private Student currentStudent(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return studentRepository.findByUserId(user.getId()).orElseThrow();
    }

    // --- 1. APPLY (Confirm Page) ---
    @GetMapping("/apply/{jobId}")
    public String showApplyConfirmPage(@PathVariable Long jobId, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);

        if (student == null || !student.isProfileComplete()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please complete your profile and upload a CV before applying.");
            return "redirect:/student/profile";
        }

        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        model.addAttribute("job", job);
        return "student/apply-confirm";
    }

    // --- 2. APPLY (Process to DB) ---
    @PostMapping("/apply/{jobId}")
    public String processApply(@PathVariable Long jobId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElseThrow();
        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();

        try {
            applicationService.applyForJob(student, job);
            redirectAttributes.addFlashAttribute("msg", "Successfully applied for " + job.getTitle() + "!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/student/dashboard";
    }

    // --- 3. STUDENT: MY APPLICATIONS ---
    @GetMapping("/my")
    public String showMyApplications(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElseThrow();

        List<Application> applications = applicationRepository.findByStudentId(student.getId(), Pageable.unpaged()).getContent();
        model.addAttribute("applications", applications);
        return "student/applications";
    }

    // --- 4. STUDENT: WITHDRAW ---
    @PostMapping("/{id}/withdraw")
    public String withdrawApplication(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(authentication);
        Application app = applicationRepository.findById(id).orElseThrow();
        if (!app.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("This application does not belong to you.");
        }
        try {
            applicationService.withdrawApplication(id);
            redirectAttributes.addFlashAttribute("msg", "Application withdrawn successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/applications/my";
    }

    // --- 5. COMPANY: REVIEW APPLICATION ---
    @GetMapping("/review/{id}")
    public String reviewApplication(@PathVariable Long id, Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        Application app = applicationRepository.findById(id).orElseThrow();
        if (!app.getJobPosting().getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("This application does not belong to your company.");
        }
        model.addAttribute("companyName", company.getName());
        model.addAttribute("applicationId", id);
        model.addAttribute("app", app);
        return "company/application-review";
    }

    // --- 6. COMPANY: UPDATE STATUS ---
    // Optional "redirectTo" lets different pages (applicants list, CV detail,
    // review page, shortlist page) send the company back to where they were.
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status,
                               @RequestParam(required = false) String redirectTo,
                               Authentication authentication, RedirectAttributes redirectAttributes) {
        Company company = currentCompany(authentication);
        Application app = applicationRepository.findById(id).orElseThrow();
        if (!app.getJobPosting().getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("This application does not belong to your company.");
        }

        String email = authentication.getName();
        applicationService.updateStatus(id, status, email);
        redirectAttributes.addFlashAttribute("msg", "Application Status Updated to: " + status);

        if (redirectTo != null && !redirectTo.isBlank()) {
            // Only allow redirecting within our own app's company pages.
            if (redirectTo.startsWith("/company/") || redirectTo.startsWith("/applications/")) {
                return "redirect:" + redirectTo;
            }
        }
        return "redirect:/applications/review/" + id;
    }
}
