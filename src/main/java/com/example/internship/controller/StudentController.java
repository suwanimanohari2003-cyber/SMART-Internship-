package com.example.internship.controller;

import com.example.internship.model.Application;
import com.example.internship.model.InterviewSession;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.model.User;
import com.example.internship.repository.ApplicationRepository;
import com.example.internship.repository.InterviewSessionRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.example.internship.service.ApplicationService;
import com.example.internship.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final InterviewSessionRepository interviewSessionRepository;

    public StudentController(StudentRepository studentRepository,
                             UserRepository userRepository,
                             FileStorageService fileStorageService,
                             JobPostingRepository jobPostingRepository,
                             ApplicationRepository applicationRepository,
                             ApplicationService applicationService,
                             InterviewSessionRepository interviewSessionRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.jobPostingRepository = jobPostingRepository;
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.interviewSessionRepository = interviewSessionRepository;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String showStudentDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);

        List<JobPosting> jobs = jobPostingRepository.findByStatus("OPEN");
        model.addAttribute("jobs", jobs);

        List<Long> appliedJobIds = List.of();
        if (student != null) {
            appliedJobIds = applicationRepository.findByStudent(student)
                .stream().map(a -> a.getJobPosting().getId())
                .collect(Collectors.toList());
        }
        model.addAttribute("appliedJobIds", appliedJobIds);
        model.addAttribute("student", student);
        return "student/dashboard";
    }

    // ── Profile GET ────────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String showStudentProfile(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(new Student());
        model.addAttribute("student", student);

        // Mock interview history + marks — shown directly on the profile page
        if (student.getId() != null) {
            try {
                List<InterviewSession> sessions = interviewSessionRepository.findByStudentId(student.getId())
                    .stream()
                    .sorted(Comparator.comparing(InterviewSession::getStartedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
                model.addAttribute("interviewSessions", sessions);

                long completedCount = sessions.stream().filter(s -> s.getCompletedAt() != null).count();
                long passedCount = sessions.stream().filter(InterviewSession::isPassed).count();
                double avgScore = sessions.stream()
                    .filter(s -> s.getCompletedAt() != null && s.getTotalScore() != null)
                    .mapToDouble(InterviewSession::getTotalScore)
                    .average().orElse(0.0);

                model.addAttribute("completedInterviewCount", completedCount);
                model.addAttribute("passedInterviewCount", passedCount);
                model.addAttribute("averageInterviewScore", Math.round(avgScore * 10.0) / 10.0);
            } catch (Exception e) {
                // Don't let a problem in interview history take down the whole
                // profile page — log it so it can be diagnosed, and show an
                // empty history instead of a 500.
                System.err.println("⚠ Failed to load interview history for student " + student.getId() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("interviewSessions", List.of());
                model.addAttribute("completedInterviewCount", 0L);
                model.addAttribute("passedInterviewCount", 0L);
                model.addAttribute("averageInterviewScore", 0.0);
            }
        } else {
            model.addAttribute("interviewSessions", List.of());
            model.addAttribute("completedInterviewCount", 0L);
            model.addAttribute("passedInterviewCount", 0L);
            model.addAttribute("averageInterviewScore", 0.0);
        }

        return "student/profile";
    }

    // ── Profile POST — saves data + CV, deletes old CV on re-upload ───────────
    @PostMapping("/profile")
    public String updateStudentProfile(Authentication authentication,
                                       @ModelAttribute Student studentForm,
                                       @RequestParam("cvFile") MultipartFile cvFile,
                                       RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(new Student());

        student.setUser(user);
        student.setEmail(user.getEmail());
        student.setFullName(studentForm.getFullName());
        student.setUniversity(studentForm.getUniversity());
        student.setGpa(studentForm.getGpa());
        student.setSkills(studentForm.getSkills());

        // CV upload — with MIME + size validation
        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                // Delete old CV if one exists
                if (student.getCvPath() != null && !student.getCvPath().isBlank()) {
                    fileStorageService.deleteFile(student.getCvPath());
                }
                String uniqueFileName = fileStorageService.saveFile(cvFile);
                student.setCvPath(uniqueFileName);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
                redirectAttributes.addFlashAttribute("student", student);
                return "redirect:/student/profile";
            }
        }

        student.setProfileComplete(true);
        studentRepository.save(student);

        redirectAttributes.addFlashAttribute("msg", "Profile and CV saved successfully!");
        return "redirect:/student/my-cv";
    }

    // ── My CV ──────────────────────────────────────────────────────────────────
    @GetMapping("/my-cv")
    public String showMyCv(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(new Student());
        model.addAttribute("student", student);
        return "student/my-cv";
    }

    // ── Download CV ────────────────────────────────────────────────────────────
    @GetMapping("/download-cv")
    public ResponseEntity<Resource> downloadCv(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();
            Student student = studentRepository.findByUserId(user.getId()).orElseThrow();
            if (student.getCvPath() == null) return ResponseEntity.notFound().build();

            java.nio.file.Path filePath = fileStorageService.getFilePath(student.getCvPath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + student.getFullName().replace(" ", "_") + "_CV.pdf\"")
                    .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Apply for job (from dashboard quick button) ────────────────────────────
    @PostMapping("/apply/{jobId}")
    public String applyForJob(@PathVariable Long jobId, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);

        if (student == null || !student.isProfileComplete()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please complete your profile before applying.");
            return "redirect:/student/profile";
        }

        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        try {
            applicationService.applyForJob(student, job);
            redirectAttributes.addFlashAttribute("msg", "Successfully applied for " + job.getTitle() + "!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/student/applications";
    }

    // ── My Applications — FIX: uses orElse(null) not orElseThrow ──────────────
    @GetMapping("/applications")
    public String showMyApplications(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        // FIX: use orElse(null) so page loads even if student profile incomplete
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);

        if (student == null) {
            model.addAttribute("applications", List.of());
            model.addAttribute("noProfile", true);
            return "student/applications";
        }

        List<Application> applications = applicationRepository.findByStudent(student);
        model.addAttribute("applications", applications);
        return "student/applications";
    }

    // ── Job Detail ─────────────────────────────────────────────────────────────
    @GetMapping("/job/{id}")
    public String showJobDetails(@PathVariable Long id, Authentication authentication, Model model) {
        JobPosting job = jobPostingRepository.findById(id).orElseThrow();
        model.addAttribute("job", job);

        // Check if already applied
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);
        boolean alreadyApplied = student != null &&
            applicationRepository.existsByStudentIdAndJobPostingId(student.getId(), id);
        model.addAttribute("alreadyApplied", alreadyApplied);
        return "student/job-details";
    }
}
