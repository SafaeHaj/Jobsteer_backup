package com.Jobsteer.Jobsteer.controllers;

import com.Jobsteer.Jobsteer.entities.*;
import com.Jobsteer.Jobsteer.services.MatchingService;
import com.Jobsteer.Jobsteer.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {
	private static final Logger logger = LoggerFactory.getLogger(MatchingController.class);
    @Autowired
    private MatchingService matchingService;
    
    @Autowired
    private JobPostRepository jobPostRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;

    @GetMapping("/jobs/{resumeId}")
    public ResponseEntity<?> findMatchingJobs(@PathVariable("resumeId") int resumeId) {
        try {
            Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
            if (!resumeOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            List<JobPost> allJobs = jobPostRepository.findAll();
            List<Map<String, Object>> matches = matchingService.findMatchingJobs(resumeOpt.get(), allJobs);

            // Format the matches with job details
            List<Map<String, Object>> formattedMatches = matches.stream()
                .map(match -> {
                    JobPost job = allJobs.stream()
                        .filter(j -> j.getId() == ((Integer)match.get("job_post_id")))
                        .findFirst()
                        .orElse(null);

                    Map<String, Object> formatted = new HashMap<>(match);
                    if (job != null) {
                        formatted.put("job_title", job.getTitle());
                        formatted.put("job_location", job.getLocation());
                    }
                    return formatted;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(formattedMatches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error finding matching jobs");
        }
    }

    @GetMapping("/candidates/{jobPostId}")
    public ResponseEntity<?> findMatchingCandidates(@PathVariable int jobPostId) {
        try {
            Optional<JobPost> jobPostOpt = jobPostRepository.findById(jobPostId);
            if (!jobPostOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Resume> allResumes = resumeRepository.findAll();
            List<Map<String, Object>> matches = matchingService.findMatchingCandidates(jobPostOpt.get(), allResumes);
            
            return ResponseEntity.ok(matches);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error finding matching candidates: " + e.getMessage());
        }
    }
}