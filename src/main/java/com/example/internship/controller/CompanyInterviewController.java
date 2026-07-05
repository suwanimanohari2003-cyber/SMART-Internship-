package com.example.internship.controller;

import com.example.internship.model.Company;
import com.example.internship.model.InterviewQuestion;
import com.example.internship.model.JobPosting;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.InterviewQuestionRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/company/jobs/{jobId}/questions")
public class CompanyInterviewController {

    private final JobPostingRepository jobPostingRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyInterviewController(JobPostingRepository jobPostingRepository,
                                       InterviewQuestionRepository interviewQuestionRepository,
                                       CompanyRepository companyRepository,
                                       UserRepository userRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    private Company currentCompany(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName()).orElseThrow().getId();
        return companyRepository.findByUserId(userId).orElseThrow();
    }

    private JobPosting loadOwnedJob(Long jobId, Company company) {
        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        if (job.getCompany() == null || !job.getCompany().getId().equals(company.getId())) {
            throw new AccessDeniedException("This job posting does not belong to your company.");
        }
        return job;
    }

    @GetMapping
    public String manageQuestions(@PathVariable Long jobId, Authentication authentication, Model model) {
        Company company = currentCompany(authentication);
        JobPosting job = loadOwnedJob(jobId, company);
        List<InterviewQuestion> questions = interviewQuestionRepository.findByJobPostingId(jobId);

        model.addAttribute("companyName", company.getName());
        model.addAttribute("job", job);
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new InterviewQuestion());

        long technicalCount = questions.stream().filter(q -> "TECHNICAL".equalsIgnoreCase(q.getCategory())).count();
        long hrCount = questions.stream().filter(q -> "HR".equalsIgnoreCase(q.getCategory())).count();
        long aptitudeCount = questions.stream().filter(q -> "APTITUDE".equalsIgnoreCase(q.getCategory())).count();
        model.addAttribute("technicalCount", technicalCount);
        model.addAttribute("hrCount", hrCount);
        model.addAttribute("aptitudeCount", aptitudeCount);

        return "company/job-questions";
    }

    @PostMapping("/add")
    public String addQuestion(@PathVariable Long jobId, @ModelAttribute InterviewQuestion newQuestion,
                               Authentication authentication, RedirectAttributes redirectAttributes) {
        Company company = currentCompany(authentication);
        JobPosting job = loadOwnedJob(jobId, company);
        newQuestion.setJobPosting(job);
        interviewQuestionRepository.save(newQuestion);

        redirectAttributes.addFlashAttribute("msg", "Interview question added successfully!");
        return "redirect:/company/jobs/" + jobId + "/questions";
    }

    @PostMapping("/{qId}/update")
    public String updateQuestion(@PathVariable Long jobId, @PathVariable Long qId,
                                  @ModelAttribute InterviewQuestion updatedQuestion,
                                  Authentication authentication, RedirectAttributes redirectAttributes) {
        Company company = currentCompany(authentication);
        JobPosting job = loadOwnedJob(jobId, company);

        InterviewQuestion existing = interviewQuestionRepository.findById(qId).orElseThrow();
        if (existing.getJobPosting() == null || !existing.getJobPosting().getId().equals(job.getId())) {
            throw new AccessDeniedException("This question does not belong to this job posting.");
        }

        existing.setQuestionText(updatedQuestion.getQuestionText());
        existing.setCategory(updatedQuestion.getCategory());
        existing.setDifficulty(updatedQuestion.getDifficulty());
        existing.setSampleAnswer(updatedQuestion.getSampleAnswer());
        interviewQuestionRepository.save(existing);

        redirectAttributes.addFlashAttribute("msg", "Interview question updated successfully!");
        return "redirect:/company/jobs/" + jobId + "/questions";
    }

    @PostMapping("/{qId}/delete")
    public String deleteQuestion(@PathVariable Long jobId, @PathVariable Long qId,
                                  Authentication authentication, RedirectAttributes redirectAttributes) {
        Company company = currentCompany(authentication);
        JobPosting job = loadOwnedJob(jobId, company);

        InterviewQuestion existing = interviewQuestionRepository.findById(qId).orElseThrow();
        if (existing.getJobPosting() == null || !existing.getJobPosting().getId().equals(job.getId())) {
            throw new AccessDeniedException("This question does not belong to this job posting.");
        }

        interviewQuestionRepository.deleteById(qId);
        redirectAttributes.addFlashAttribute("msg", "Question deleted successfully.");
        return "redirect:/company/jobs/" + jobId + "/questions";
    }
}
