package com.leetcoder.web.controller;

import com.leetcoder.application.service.SpacedRepetitionService;
import com.leetcoder.web.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final SpacedRepetitionService spacedRepetitionService;

    @PostMapping
    public ResponseEntity<String> submitReview(@RequestBody ReviewRequest request) {
        if (request.studyItemId() == null) {
            return ResponseEntity.badRequest().body("StudyItemId cannot be null");
        }

        try {
            spacedRepetitionService.processReview(request.studyItemId(), request.quality());
            return ResponseEntity.ok("Review processed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
