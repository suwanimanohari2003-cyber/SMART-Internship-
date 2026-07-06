package com.example.internship.dto;

import java.util.List;

public class CvRankResult {

    private Long studentId;
    private String studentName;
    private double matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;

    public CvRankResult(Long studentId, String studentName, double matchScore, List<String> matchedSkills, List<String> missingSkills) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
    }

    // --- Getters and Setters ---
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
}