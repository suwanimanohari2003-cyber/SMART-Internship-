package com.example.internship.service;

import com.example.internship.model.JobPosting;
import com.example.internship.repository.JobPostingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SchedulerService {

    private final JobPostingRepository jobPostingRepository;

    public SchedulerService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseExpiredJobs() {
        System.out.println("Running Midnight Scheduler: Checking for expired Job Postings...");

        LocalDate today = LocalDate.now();
        List<JobPosting> expiredJobs = jobPostingRepository.findByDeadlineBeforeAndStatus(today, "OPEN");

        int closedCount = 0;
        for (JobPosting job : expiredJobs) {
            job.setStatus("CLOSED");
            jobPostingRepository.save(job);
            closedCount++;
        }

        System.out.println("Scheduler Finished: " + closedCount + " expired jobs were automatically closed.");
    }
}
