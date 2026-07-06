package com.example.internship.service;

import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import com.example.internship.model.InterviewSession;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Member 9 — EmailService
 * @Async so emails never block the HTTP request thread.
 * Sends emails for: application confirmed, status changed, interview score ready.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Sent when student clicks "Confirm & Apply" */
    @Async
    public void sendApplicationConfirmation(Student student, JobPosting job) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(student.getEmail());
            msg.setSubject("✅ Application Received: " + job.getTitle() + " at " + job.getCompany().getName());
            msg.setText(
                "Dear " + student.getFullName() + ",\n\n" +
                "Congratulations! Your application has been successfully submitted.\n\n" +
                "📋 Application Details:\n" +
                "   Position : " + job.getTitle() + "\n" +
                "   Company  : " + job.getCompany().getName() + "\n" +
                "   Location : " + (job.getCompany().getLocation() != null ? job.getCompany().getLocation() : "Sri Lanka") + "\n" +
                "   Deadline : " + job.getDeadline() + "\n\n" +
                "The company will review your profile and CV. You will receive another email once a decision is made.\n\n" +
                "Best of luck!\n" +
                "Smart Internship Portal Team"
            );
            mailSender.send(msg);
            System.out.println("✉ Email sent: Application Confirmation → " + student.getEmail());
        } catch (Exception e) {
            logMailFailure("ApplicationConfirmation", e);
        }
    }

    /**
     * Sent when company updates status.
     * Sends a tailored email for ACCEPTED, REJECTED, and SHORTLISTED.
     */
    @Async
    public void sendStatusChangeAlert(Student student, JobPosting job, String newStatus) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(student.getEmail());

            switch (newStatus.toUpperCase()) {
                case "ACCEPTED" -> {
                    msg.setSubject("🎉 Great News! Your Application Was Accepted — " + job.getTitle());
                    msg.setText(
                        "Dear " + student.getFullName() + ",\n\n" +
                        "We are thrilled to inform you that your application for '" + job.getTitle() +
                        "' at " + job.getCompany().getName() + " has been ACCEPTED! 🎉\n\n" +
                        "The company will contact you shortly to arrange the next steps.\n\n" +
                        "Please log in to the Smart Internship Portal and check your Applications page for interview scheduling details.\n\n" +
                        "Well done and congratulations!\n" +
                        "Smart Internship Portal Team"
                    );
                }
                case "REJECTED" -> {
                    msg.setSubject("Application Update: " + job.getTitle() + " at " + job.getCompany().getName());
                    msg.setText(
                        "Dear " + student.getFullName() + ",\n\n" +
                        "We regret to inform you that your application for '" + job.getTitle() +
                        "' at " + job.getCompany().getName() + " was not successful at this time.\n\n" +
                        "Please don't be discouraged — there are many other opportunities available on the portal. " +
                        "Keep applying and improving your profile!\n\n" +
                        "We wish you the very best in your internship search.\n\n" +
                        "Warm regards,\n" +
                        "Smart Internship Portal Team"
                    );
                }
                case "SHORTLISTED" -> {
                    msg.setSubject("⭐ You've Been Shortlisted! — " + job.getTitle());
                    msg.setText(
                        "Dear " + student.getFullName() + ",\n\n" +
                        "Excellent news! You have been SHORTLISTED for the '" + job.getTitle() +
                        "' position at " + job.getCompany().getName() + "! ⭐\n\n" +
                        "Your mock interview for this position is now unlocked. " +
                        "Log in to your portal and go to Applications → Start Mock Interview to prepare.\n\n" +
                        "Good luck!\n" +
                        "Smart Internship Portal Team"
                    );
                }
                default -> {
                    msg.setSubject("Application Status Update: " + job.getTitle());
                    msg.setText(
                        "Dear " + student.getFullName() + ",\n\n" +
                        "Your application status for '" + job.getTitle() + "' at " +
                        job.getCompany().getName() + " has been updated to: " + newStatus + ".\n\n" +
                        "Log in to the portal for more details.\n\n" +
                        "Smart Internship Portal Team"
                    );
                }
            }

            mailSender.send(msg);
            System.out.println("✉ Email sent: Status=" + newStatus + " → " + student.getEmail());
        } catch (Exception e) {
            logMailFailure("StatusAlert", e);
        }
    }

    /** Sent when interview session is completed and score is available */
    @Async
    public void sendInterviewScoreReady(Student student, InterviewSession session) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(student.getEmail());
            msg.setSubject("📊 Mock Interview Results Ready — " + session.getJobPosting().getTitle());
            msg.setText(
                "Dear " + student.getFullName() + ",\n\n" +
                "Your mock interview results are ready!\n\n" +
                "📊 Your Score: " + session.getTotalScore() + "%\n" +
                "   Result  : " + (session.isPassed() ? "PASSED ✅" : "FAILED ❌") + "\n" +
                "   Position: " + session.getJobPosting().getTitle() + "\n" +
                "   Company : " + session.getJobPosting().getCompany().getName() + "\n\n" +
                "Log in to your dashboard to view detailed AI feedback for each answer.\n\n" +
                "Best regards,\n" +
                "Smart Internship Portal Team"
            );
            mailSender.send(msg);
            System.out.println("✉ Email sent: Interview Score → " + student.getEmail());
        } catch (Exception e) {
            logMailFailure("InterviewScore", e);
        }
    }

    /** Sent when company sets a physical interview date after ACCEPTING */
    @Async
    public void sendPhysicalInterviewScheduled(Student student, JobPosting job, String interviewDate, String venue) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(student.getEmail());
            msg.setSubject("📅 Physical Interview Scheduled — " + job.getTitle() + " at " + job.getCompany().getName());
            msg.setText(
                "Dear " + student.getFullName() + ",\n\n" +
                "Your physical interview has been scheduled! Please find the details below:\n\n" +
                "📅 Interview Details:\n" +
                "   Position : " + job.getTitle() + "\n" +
                "   Company  : " + job.getCompany().getName() + "\n" +
                "   Date     : " + interviewDate + "\n" +
                "   Venue    : " + (venue != null ? venue : job.getCompany().getLocation()) + "\n\n" +
                "Please arrive 15 minutes early and bring a printed copy of your CV.\n\n" +
                "Best of luck!\n" +
                "Smart Internship Portal Team"
            );
            mailSender.send(msg);
            System.out.println("✉ Email sent: Physical Interview Scheduled → " + student.getEmail());
        } catch (Exception e) {
            logMailFailure("PhysicalInterview", e);
        }
    }

    /**
     * Centralised mail-failure logging. Gmail SMTP rejects the regular
     * account password — it requires a 16-character "App Password"
     * (Google Account -> Security -> 2-Step Verification -> App Passwords).
     * This prints a clear, actionable message instead of a bare stack
     * trace so a bad spring.mail.password in application.properties is
     * obvious instead of silently swallowed.
     */
    private void logMailFailure(String context, Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        boolean looksLikeAuthFailure = msg.contains("535") || msg.toLowerCase().contains("authentication failed")
            || msg.toLowerCase().contains("username and password not accepted");
        System.err.println("\u2709 Email failed (" + context + "): " + e.getClass().getSimpleName() + " - " + msg);
        if (looksLikeAuthFailure) {
            System.err.println("   \u26A0 This looks like a Gmail authentication failure. spring.mail.password " +
                "must be a 16-character Gmail App Password (Google Account > Security > 2-Step Verification > " +
                "App Passwords), NOT your normal Gmail login password. Update spring.mail.username / " +
                "spring.mail.password in application.properties and restart the app.");
        }
    }
}
