package com.example.internship.controller;

import com.example.internship.model.*;
import com.example.internship.repository.*;
import com.example.internship.service.InterviewService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/student/interviews")
public class StudentInterviewController {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final JobPostingRepository jobPostingRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final InterviewService interviewService;
    private final ApplicationRepository applicationRepository;

    public StudentInterviewController(InterviewSessionRepository sessionRepository, InterviewQuestionRepository questionRepository,
                                      InterviewAnswerRepository answerRepository, JobPostingRepository jobPostingRepository,
                                      StudentRepository studentRepository, UserRepository userRepository, InterviewService interviewService,
                                      ApplicationRepository applicationRepository) {
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.interviewService = interviewService;
        this.applicationRepository = applicationRepository;
    }

    private Student currentStudent(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return studentRepository.findByUserId(user.getId()).orElseThrow();
    }

    private InterviewSession loadOwnedSession(Long sessionId, Student student) {
        InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();
        if (!session.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("This interview session does not belong to you.");
        }
        return session;
    }

    @PostMapping("/start/{jobId}")
    public String startInterview(@PathVariable Long jobId, Authentication authentication, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(authentication);
        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();

        // Only allow starting once the student's application for this job has
        // been shortlisted by the company — matches the "unlock" UX on the
        // Applications page and prevents skipping the review step.
        Application application = applicationRepository
            .findByStudentIdAndJobPostingId(student.getId(), jobId)
            .orElse(null);
        boolean unlocked = application != null
            && ("SHORTLISTED".equals(application.getStatus()) || "ACCEPTED".equals(application.getStatus()));
        if (!unlocked) {
            redirectAttributes.addFlashAttribute("errorMsg",
                "Mock interview is locked until your application is shortlisted by the company.");
            return "redirect:/student/applications";
        }

        java.util.Optional<InterviewSession> existing =
            sessionRepository.findFirstByStudentIdAndJobPostingId(student.getId(), jobId);
        if (existing.isPresent()) {
            InterviewSession s = existing.get();
            if (s.getCompletedAt() != null) {
                return "redirect:/student/interviews/" + s.getId() + "/result";
            }
            return "redirect:/student/interviews/" + s.getId() + "/question/0";
        }

        InterviewSession session = new InterviewSession();
        session.setStudent(student);
        session.setJobPosting(job);
        session.setStartedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return "redirect:/student/interviews/" + session.getId() + "/question/0";
    }

    @GetMapping("/{sessionId}/question/{index}")
    public String showQuestion(@PathVariable Long sessionId, @PathVariable int index,
                               Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Student student = currentStudent(authentication);
        InterviewSession session = loadOwnedSession(sessionId, student);

        if (session.getCompletedAt() != null) {
            return "redirect:/student/interviews/" + sessionId + "/result";
        }

        if (session.getJobPosting() == null) {
            redirectAttributes.addFlashAttribute("errorMsg",
                "This interview session is no longer linked to a job posting. Please contact support.");
            return "redirect:/student/applications";
        }

        List<InterviewQuestion> questions;
        try {
            questions = questionRepository.findByJobPostingId(session.getJobPosting().getId());
        } catch (Exception e) {
            System.err.println("⚠ Failed to load interview questions for session " + sessionId + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg",
                "Could not load interview questions right now. Please try again.");
            return "redirect:/student/applications";
        }

        if (questions == null || questions.isEmpty()) {
            // No questions configured for this job yet — nothing to finalize against.
            model.addAttribute("interviewSession", session);
            model.addAttribute("noQuestions", true);
            return "student/mock-interview";
        }

        if (index < 0 || index >= questions.size()) {
            if (index >= questions.size()) {
                try {
                    interviewService.finalizeSession(sessionId);
                } catch (Exception e) {
                    System.err.println("⚠ Failed to finalize interview session " + sessionId + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("errorMsg",
                        "Could not finalize your interview results. Please contact support.");
                    return "redirect:/student/applications";
                }
                return "redirect:/student/interviews/" + sessionId + "/result";
            }
            return "redirect:/student/interviews/" + sessionId + "/question/0";
        }

        model.addAttribute("interviewSession", session);
        model.addAttribute("question", questions.get(index));
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("noQuestions", false);

        return "student/mock-interview";
    }

    @PostMapping("/{sessionId}/answer/{index}")
    public String submitAnswer(@PathVariable Long sessionId, @PathVariable int index,
                               @RequestParam Long questionId, @RequestParam String answerText,
                               Authentication authentication) {

        Student student = currentStudent(authentication);
        InterviewSession session = loadOwnedSession(sessionId, student);
        if (session.getCompletedAt() != null) {
            return "redirect:/student/interviews/" + sessionId + "/result";
        }

        InterviewQuestion question = questionRepository.findById(questionId).orElseThrow();

        InterviewAnswer answer = new InterviewAnswer();
        answer.setSession(session);
        answer.setQuestion(question);
        answer.setAnswerText(answerText);

        Double score = interviewService.calculateScore(answerText, question.getSampleAnswer(), question.getCategory());
        answer.setScore(score);

        if (score >= 7.5) answer.setFeedback("Excellent! You covered the key concepts clearly.");
        else if (score >= 5.5) answer.setFeedback("Good answer overall — solid understanding shown.");
        else if (score >= 3.5) answer.setFeedback("Decent attempt, but try to mention more specific terms and details related to this topic.");
        else answer.setFeedback("Needs improvement. Review the core concepts related to this topic.");

        answerRepository.save(answer);

        return "redirect:/student/interviews/" + sessionId + "/question/" + (index + 1);
    }

    @GetMapping("/my")
    public String myInterviews(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        try {
            List<InterviewSession> sessions = sessionRepository.findByStudentId(student.getId());
            model.addAttribute("sessions", sessions);
        } catch (Exception e) {
            System.err.println("⚠ Failed to load interview sessions for student " + student.getId() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("sessions", List.of());
        }
        return "student/my-interviews";
    }

    @GetMapping("/{sessionId}/result")
    public String showResult(@PathVariable Long sessionId, Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        InterviewSession session = loadOwnedSession(sessionId, student);
        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);

        model.addAttribute("interviewSession", session);
        model.addAttribute("answers", answers);
        return "student/interview-result";
    }
}
