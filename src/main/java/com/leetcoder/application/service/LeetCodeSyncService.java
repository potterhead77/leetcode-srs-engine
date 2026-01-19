package com.leetcoder.application.service;

import com.leetcoder.domain.entity.Question;
import com.leetcoder.domain.entity.StudyItem;
import com.leetcoder.domain.entity.User;
import com.leetcoder.infrastructure.client.LeetCodeClient;
import com.leetcoder.infrastructure.client.LeetCodeClient.QuestionDto;
import com.leetcoder.infrastructure.client.LeetCodeClient.SubmissionDto;
import com.leetcoder.infrastructure.repository.QuestionRepository;
import com.leetcoder.infrastructure.repository.StudyItemRepository;
import com.leetcoder.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeetCodeSyncService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final StudyItemRepository studyItemRepository;
    private final LeetCodeClient leetCodeClient;
    private final SpacedRepetitionService sm2Service;

    // Run every 6 hours
    @Scheduled(cron = "${app.sync.cron:0 0 */6 * * *}")
    public void syncAllUsers() {
        log.info("Starting LeetCode Sync...");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                syncUser(user);
            } catch (Exception e) {
                log.error("Failed to sync user: {}", user.getLeetcodeUsername(), e);
            }
        }
        log.info("LeetCode Sync Completed.");
    }

    // Public so it can be triggered manually if needed
    @Transactional
    public void syncUser(User user) {
        log.info("Syncing user: {}", user.getLeetcodeUsername());
        List<SubmissionDto> submissions = leetCodeClient.getRecentSubmissions(user.getLeetcodeUsername());

        for (SubmissionDto sub : submissions) {
            try {
                processSubmission(user, sub);
            } catch (Exception e) {
                log.error("Error processing submission {} for user {}", sub.titleSlug(), user.getLeetcodeUsername(), e);
            }
        }
    }

    private void processSubmission(User user, SubmissionDto sub) {
        String titleSlug = sub.titleSlug();
        if (titleSlug == null) {
            log.warn("Submission {} has no titleSlug, skipping", sub.id());
            return;
        }

        // 1. Lazy Load Question
        Question question = questionRepository.findById(titleSlug).orElseGet(() -> {
            QuestionDto qDetails = leetCodeClient.getQuestionDetails(titleSlug);
            if (qDetails == null) {
                // Fallback if details fetch fails: create a skeleton
                return Question.builder()
                        .titleSlug(titleSlug)
                        .title(sub.title())
                        .difficulty("Unknown") // Or handle later
                        .url("https://leetcode.com/problems/" + titleSlug)
                        .build();
            }
            // Save new question
            Question newQ = Question.builder()
                    .titleSlug(qDetails.titleSlug())
                    .title(qDetails.title())
                    .difficulty(qDetails.difficulty())
                    .url("https://leetcode.com/problems/" + qDetails.titleSlug())
                    .build();
            @SuppressWarnings("null")
            Question saved = questionRepository.save(newQ);
            return saved;
        });

        if (question == null)
            return; // Should not happen due to orElseGet logic but for safety

        // 2. Parse Timestamp
        long epochSeconds = Long.parseLong(sub.timestamp());
        LocalDateTime submissionTime = LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC);

        // 3. Find or Create StudyItem
        Optional<StudyItem> existingItemOpt = studyItemRepository.findByUserAndQuestionTitleSlug(user, titleSlug);

        if (existingItemOpt.isEmpty()) {
            // Scenario A: New Item (Implicit Review)
            createFirstReview(user, question, submissionTime);
        } else {
            // Scenario B: Existing Item
            StudyItem item = existingItemOpt.get();
            if (item.getLastReviewedAt() == null || submissionTime.isAfter(item.getLastReviewedAt())) {
                // User solved it again implies review
                updateReview(item, submissionTime);
            }
        }
    }

    private void createFirstReview(User user, Question question, LocalDateTime reviewedAt) {
        // Quality 4 for implicit first success
        SpacedRepetitionService.ReviewResult result = sm2Service.calculateNextReview(2.5, 0, 0, 4);

        StudyItem item = StudyItem.builder()
                .user(user)
                .question(question)
                .easeFactor(result.easeFactor)
                .intervalDays(result.intervalDays)
                .repetitions(result.repetitions)
                .lastReviewedAt(reviewedAt)
                .nextReviewAt(result.nextReviewAt)
                .build();

        @SuppressWarnings({ "null", "unused" })
        StudyItem saved = studyItemRepository.save(item);
        log.info("Created new StudyItem for user {} - {}", user.getLeetcodeUsername(), question.getTitleSlug());
    }

    private void updateReview(StudyItem item, LocalDateTime reviewedAt) {
        // Quality 4 for implicit re-solve
        SpacedRepetitionService.ReviewResult result = sm2Service.calculateNextReview(
                item.getEaseFactor(),
                item.getIntervalDays(),
                item.getRepetitions(),
                4);

        item.setEaseFactor(result.easeFactor);
        item.setIntervalDays(result.intervalDays);
        item.setRepetitions(result.repetitions);
        item.setLastReviewedAt(reviewedAt);
        item.setNextReviewAt(result.nextReviewAt);

        // storage is not null
        @SuppressWarnings({ "null", "unused" })
        StudyItem saved = studyItemRepository.save(item);
        log.info("Updated StudyItem for user {} - {}", item.getUser().getLeetcodeUsername(),
                item.getQuestion().getTitleSlug());
    }
}
