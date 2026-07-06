package com.example.internship.specification;

import com.example.internship.model.JobPosting;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class JobPostingSpecification {

    public static Specification<JobPosting> filterJobs(String keyword, String industry, String location, Double minGpa) {
        return (root, query, criteriaBuilder) -> {

            Specification<JobPosting> spec = (r, q, cb) -> cb.conjunction();

            // 1. Filter by Keyword (Title or Description)
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                spec = spec.and((r, q, cb) -> cb.or(
                        cb.like(cb.lower(r.get("title")), likeKeyword),
                        cb.like(cb.lower(r.get("description")), likeKeyword)
                ));
            }

            // 2. Filter by Company Industry
            if (StringUtils.hasText(industry)) {
                spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("company").get("industry")), industry.toLowerCase()));
            }

            // 3. Filter by Company Location
            if (StringUtils.hasText(location)) {
                String likeLocation = "%" + location.toLowerCase() + "%";
                spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("company").get("location")), likeLocation));
            }

            // 4. Filter by Minimum GPA
            if (minGpa != null) {
                spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("minGpa"), minGpa));
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}