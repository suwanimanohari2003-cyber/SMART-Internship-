package com.example.internship.service;

import com.example.internship.dto.CvRankResult;
import com.example.internship.model.JobPosting;
import com.example.internship.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CvFilterService {

    private final CvKeywordExtractor keywordExtractor;

    @Value("${cv.filter.threshold:60.0}")
    private double shortlistThreshold;

    public CvFilterService(CvKeywordExtractor keywordExtractor) {
        this.keywordExtractor = keywordExtractor;
    }

    public CvRankResult rankStudent(JobPosting job, Student student) {
        List<String> requiredSkills = keywordExtractor.extractAndNormalize(job.getRequiredSkills());
        List<String> studentSkills = keywordExtractor.extractAndNormalize(student.getSkills());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        if (requiredSkills.isEmpty()) {
            return new CvRankResult(student.getId(), student.getFullName(), 100.0, new ArrayList<>(), new ArrayList<>());
        }

        for (String reqSkill : requiredSkills) {
            if (studentSkills.contains(reqSkill)) {
                matchedSkills.add(reqSkill);
            } else {
                missingSkills.add(reqSkill);
            }
        }

        double matchScore = ((double) matchedSkills.size() / requiredSkills.size()) * 100.0;
        matchScore = Math.round(matchScore * 100.0) / 100.0;

        return new CvRankResult(student.getId(), student.getFullName(), matchScore, matchedSkills, missingSkills);
    }

    public List<CvRankResult> rankApplicants(JobPosting job, List<Student> applicants) {
        List<CvRankResult> results = new ArrayList<>();
        for (Student student : applicants) {
            if (student.getSkills() != null && !student.getSkills().isEmpty()) {
                results.add(rankStudent(job, student));
            }
        }
        results.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return results;
    }
}
