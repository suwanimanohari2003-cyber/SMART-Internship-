package com.example.internship.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Map;

@Service
public class ReportService {

    private final AnalyticsService analyticsService;

    public ReportService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public byte[] generateCsvReport() {
        try (StringWriter writer = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(writer)) {

            Map<String, Object> metrics = analyticsService.getSystemMetrics();

            csvWriter.writeNext(new String[]{"Metric Name", "Value"});

            csvWriter.writeNext(new String[]{"Total Students", metrics.get("totalStudents").toString()});
            csvWriter.writeNext(new String[]{"Total Companies", metrics.get("totalCompanies").toString()});
            csvWriter.writeNext(new String[]{"Active Jobs", metrics.get("totalJobs").toString()});
            csvWriter.writeNext(new String[]{"Total Applications", metrics.get("totalApplications").toString()});
            csvWriter.writeNext(new String[]{"Interview Pass Rate (%)", metrics.get("passRate").toString()});

            return writer.toString().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    public byte[] generatePdfReport() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("Smart Internship Portal - System Report", titleFont));
            document.add(new Paragraph(" ")); // හිස් පේළියක්

            Map<String, Object> metrics = analyticsService.getSystemMetrics();

            document.add(new Paragraph("Total Students: " + metrics.get("totalStudents"), normalFont));
            document.add(new Paragraph("Total Companies: " + metrics.get("totalCompanies"), normalFont));
            document.add(new Paragraph("Active Jobs: " + metrics.get("totalJobs"), normalFont));
            document.add(new Paragraph("Total Applications: " + metrics.get("totalApplications"), normalFont));
            document.add(new Paragraph("Interview Pass Rate: " + metrics.get("passRate") + "%", normalFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }
}
