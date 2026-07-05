package com.example.internship.controller;

import com.example.internship.model.User;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.JobPostingRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    public AdminController(StudentRepository studentRepository,
                           CompanyRepository companyRepository,
                           JobPostingRepository jobPostingRepository,
                           UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        long totalStudents = studentRepository.count();
        model.addAttribute("totalStudents", totalStudents);

        long totalCompanies = companyRepository.count();
        model.addAttribute("totalCompanies", totalCompanies);

        long activeJobs = jobPostingRepository.countByStatus("OPEN");
        model.addAttribute("activeJobs", activeJobs);

        model.addAttribute("interviewPassRate", "78.5%");

        List<Integer> trendData = Arrays.asList(0, 0, 0, 0, 2, (int) totalStudents + (int) totalCompanies);
        model.addAttribute("trendData", trendData);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/students")
    public String manageStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id);
        return "redirect:/admin/students";
    }

    @GetMapping("/companies")
    public String manageCompanies(Model model) {
        model.addAttribute("companies", companyRepository.findAll());
        return "admin/companies";
    }

    @PostMapping("/companies/delete/{id}")
    public String deleteCompany(@PathVariable Long id) {
        companyRepository.deleteById(id);
        return "redirect:/admin/companies";
    }

    @GetMapping("/jobs")
    public String manageJobs(Model model) {
        model.addAttribute("jobs", jobPostingRepository.findAll());
        return "admin/jobs";
    }

    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(@PathVariable Long id) {
        jobPostingRepository.deleteById(id);
        return "redirect:/admin/jobs";
    }

    @GetMapping("/reports")
    public String systemReports(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalCompanies", companyRepository.count());
        model.addAttribute("activeJobs", jobPostingRepository.countByStatus("OPEN"));
        return "admin/reports";
    }

    @GetMapping("/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"system_report.csv\"");

        long totalStudents = studentRepository.count();
        long totalCompanies = companyRepository.count();
        long activeJobs = jobPostingRepository.countByStatus("OPEN");

        String csvContent = "Metric,Value\n" +
                "Total Registered Students," + totalStudents + "\n" +
                "Total Registered Companies," + totalCompanies + "\n" +
                "Total Active Job Postings," + activeJobs + "\n" +
                "Average Interview Pass Rate,78.5%\n";

        response.getWriter().write(csvContent);
    }

    @GetMapping("/export/pdf")
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"system_report.pdf\"");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new Paragraph("Smart Internship System Report"));
        document.add(new Paragraph("----------------------------------"));
        document.add(new Paragraph("Total Registered Students: " + studentRepository.count()));
        document.add(new Paragraph("Total Registered Companies: " + companyRepository.count()));
        document.add(new Paragraph("Total Active Job Postings: " + jobPostingRepository.countByStatus("OPEN")));

        document.close();
    }

    @GetMapping("/settings")
    public String adminSettings(Model model) {
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String fullName, @RequestParam String email) {
        return "redirect:/admin/settings?success";
    }

    @GetMapping("/api-docs")
    public String showApiDocs() {
        return "admin/api-docs";
    }
}
