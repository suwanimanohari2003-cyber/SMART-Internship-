package com.example.internship.service;

import com.example.internship.model.InterviewAnswer;
import com.example.internship.model.InterviewSession;
import com.example.internship.repository.InterviewAnswerRepository;
import com.example.internship.repository.InterviewSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewAnswerRepository answerRepository;
    private final EmailService emailService;

    public InterviewService(InterviewSessionRepository sessionRepository, InterviewAnswerRepository answerRepository, EmailService emailService) {
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.emailService = emailService;
    }

    public Double calculateScore(String studentAnswer, String sampleAnswer) {
        return calculateScore(studentAnswer, sampleAnswer, "TECHNICAL");
    }

    /**
     * Heuristic scorer (no external AI call available). Two strategies:
     *
     *  - TECHNICAL / APTITUDE: the seed sample_answer is a concrete model
     *    answer with real terminology, so we reward keyword overlap. Score
     *    is a blend of keyword-match% and a base "effort" credit, so a
     *    reasonably written, correct-but-differently-worded answer doesn't
     *    score near zero just because it doesn't reuse the exact vocabulary.
     *
     *  - HR: the seed sample_answer for these questions is a *rubric*
     *    describing what a good answer should cover (e.g. "describe the
     *    situation, the challenge, the steps taken, the outcome"), not a
     *    model answer with matchable keywords. Scoring those by literal
     *    keyword overlap against the rubric text is meaningless, so HR
     *    answers are instead scored on structure/effort: length, and
     *    whether the answer touches on situation/action/result style
     *    language (the STAR method).
     */
    public Double calculateScore(String studentAnswer, String sampleAnswer, String category) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) return 0.0;

        String trimmedAnswer = studentAnswer.trim();
        if (trimmedAnswer.length() < 10) return 1.0; // a couple of words isn't a real attempt

        if (category != null && category.equalsIgnoreCase("HR")) {
            return scoreBehavioralAnswer(trimmedAnswer);
        }

        if (sampleAnswer == null || sampleAnswer.trim().isEmpty()) return 5.0;

        java.util.Set<String> stopWords = java.util.Set.of(
            "the", "a", "an", "is", "are", "was", "were", "and", "or", "but",
            "of", "to", "in", "on", "for", "with", "as", "by", "at", "it",
            "its", "this", "that", "can", "have", "has", "had", "be", "while",
            "both", "only", "which", "their", "all", "also", "into", "via",
            "uses", "use", "used", "manage", "you", "your", "from");

        String[] rawWords = sampleAnswer.toLowerCase().split("[^a-z0-9+#]+");
        java.util.List<String> keywords = new java.util.ArrayList<>();
        for (String w : rawWords) {
            if (w.length() > 2 && !stopWords.contains(w)) {
                keywords.add(w);
            }
        }
        if (keywords.isEmpty()) return 6.0;

        String ansLower = trimmedAnswer.toLowerCase();
        int matchCount = 0;
        for (String kw : keywords) {
            if (ansLower.contains(kw)) {
                matchCount++;
            }
        }
        double matchPercent = (double) matchCount / keywords.size();

        // Blend: 3 points baseline for writing a substantive, on-topic
        // answer (>= 25 chars), up to 7 points scaled by keyword overlap.
        // This avoids correct-but-paraphrased answers scoring near 0.
        double baseCredit = trimmedAnswer.length() >= 25 ? 3.0 : 1.5;
        double keywordCredit = Math.min(1.0, matchPercent * 1.6) * 7.0; // overlap of ~60%+ already earns full 7

        double score = baseCredit + keywordCredit;
        return Math.min(10.0, Math.round(score * 10.0) / 10.0);
    }

    private Double scoreBehavioralAnswer(String answer) {
        String lower = answer.toLowerCase();
        double score = 3.0; // baseline for a real attempt

        // Length bonus — a thoughtful behavioral answer is usually a few sentences
        int wordCount = answer.split("\\s+").length;
        if (wordCount >= 15) score += 1.5;
        if (wordCount >= 35) score += 1.5;
        if (wordCount >= 60) score += 1.0;

        // STAR-method signal words: situation/task, action, result
        String[] situationWords = {"challenge", "problem", "issue", "situation", "task", "project", "bug", "deadline"};
        String[] actionWords = {"i ", "solved", "fixed", "debugged", "researched", "implemented", "built", "worked", "reduced", "improved", "collaborat", "decided", "designed", "refactored"};
        String[] resultWords = {"result", "outcome", "successfully", "learned", "reduced", "improved", "increased", "decreased", "shipped", "delivered", "worked well", "fixed"};

        if (containsAny(lower, situationWords)) score += 1.5;
        if (containsAny(lower, actionWords)) score += 1.5;
        if (containsAny(lower, resultWords)) score += 1.5;

        return Math.min(10.0, Math.round(score * 10.0) / 10.0);
    }

    private boolean containsAny(String text, String[] needles) {
        for (String n : needles) {
            if (text.contains(n)) return true;
        }
        return false;
    }

    public void finalizeSession(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();
        List<InterviewAnswer> answers = answerRepository.findBySessionId(sessionId);

        double totalScoreEarned = 0.0;
        double maxPossibleScore = answers.size() * 10.0;

        for (InterviewAnswer ans : answers) {
            totalScoreEarned += ans.getScore();
        }

        double finalPercentage = (maxPossibleScore > 0) ? (totalScoreEarned / maxPossibleScore) * 100.0 : 0.0;

        session.setTotalScore(Math.round(finalPercentage * 10.0) / 10.0);
        session.setPassed(finalPercentage >= 60.0);
        session.setCompletedAt(LocalDateTime.now());

        sessionRepository.save(session);

        emailService.sendInterviewScoreReady(session.getStudent(), session);
    }
}
