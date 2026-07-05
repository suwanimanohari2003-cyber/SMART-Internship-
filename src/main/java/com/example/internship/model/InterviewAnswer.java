package com.example.internship.model;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_answers")
public class InterviewAnswer extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    private Double score = 0.0;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    // --- Getters and Setters ---
    public InterviewSession getSession() { return session; }
    public void setSession(InterviewSession session) { this.session = session; }

    public InterviewQuestion getQuestion() { return question; }
    public void setQuestion(InterviewQuestion question) { this.question = question; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
