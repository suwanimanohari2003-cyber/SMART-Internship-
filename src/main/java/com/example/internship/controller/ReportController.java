package com.example.internship.controller;

import com.example.internship.repository.ApplicationRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.CompanyRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;

    public ReportController(ApplicationRepository applicationRepository,
                            StudentRepository studentRepository,
                            CompanyRepository companyRepository,
                            JobPostingRepository jobPostingRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
    }

    @GetMapping("/applications.csv")
    public void exportApplicationsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"applications_report.csv\"");

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Student Name,Student Email,University,GPA,Job Title,Company,Status,Applied At\n");

        applicationRepository.findAll().forEach(app -> {
            csv.append(app.getId()).append(",")
               .append(quote(app.getStudent().getFullName())).append(",")
               .append(quote(app.getStudent().getEmail())).append(",")
               .append(quote(app.getStudent().getUniversity())).append(",")
               .append(app.getStudent().getGpa()).append(",")
               .append(quote(app.getJobPosting().getTitle())).append(",")
               .append(quote(app.getJobPosting().getCompany().getName())).append(",")
               .append(app.getStatus()).append(",")
               .append(app.getCreatedAt() != null ? app.getCreatedAt().toString() : "N/A").append("\n");
        });

        response.getWriter().write(csv.toString());
    }

    @GetMapping("/applications.pdf")
    public void exportApplicationsPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"applications_report.pdf\"");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new Paragraph("Smart Internship Portal — Applications Report"));
        document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now()));
        document.add(new Paragraph("----------------------------------------------------"));
        document.add(new Paragraph("Total Students: " + studentRepository.count()));
        document.add(new Paragraph("Total Companies: " + companyRepository.count()));
        document.add(new Paragraph("Total Job Postings: " + jobPostingRepository.count()));
        document.add(new Paragraph("Total Applications: " + applicationRepository.count()));
        document.add(new Paragraph("Active Jobs: " + jobPostingRepository.countByStatus("OPEN")));
        document.add(new Paragraph("----------------------------------------------------"));
        document.add(new Paragraph(" "));

        applicationRepository.findAll().forEach(app -> {
            try {
                document.add(new Paragraph(
                    "• " + app.getStudent().getFullName() +
                    " → " + app.getJobPosting().getTitle() +
                    " @ " + app.getJobPosting().getCompany().getName() +
                    " [" + app.getStatus() + "]"
                ));
            } catch (Exception e) { /* skip */ }
        });

        document.close();
    }

    private String quote(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
