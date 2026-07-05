package com.example.internship.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String category; // TECHNICAL, HR, APTITUDE

    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(name = "sample_answer", columnDefinition = "TEXT")
    private String sampleAnswer;

    // --- Getters and Setters ---
    public JobPosting getJobPosting() { return jobPosting; }
    public void setJobPosting(JobPosting jobPosting) { this.jobPosting = jobPosting; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getSampleAnswer() { return sampleAnswer; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }
}
