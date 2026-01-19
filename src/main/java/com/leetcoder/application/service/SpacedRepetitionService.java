package com.leetcoder.application.service;

import com.leetcoder.domain.entity.StudyItem;
import com.leetcoder.infrastructure.repository.StudyItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final StudyItemRepository studyItemRepository;

    @Transactional
    public void processReview(Long studyItemId, int quality) {
        if (studyItemId == null) {
            throw new IllegalArgumentException("StudyItemId cannot be null");
        }
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        StudyItem item = studyItemRepository.findById(studyItemId)
                .orElseThrow(() -> new IllegalArgumentException("StudyItem not found with ID: " + studyItemId));

        ReviewResult result = calculateNextReview(
                item.getEaseFactor(),
                item.getIntervalDays(),
                item.getRepetitions(),
                quality);

        item.setEaseFactor(result.easeFactor);
        item.setIntervalDays(result.intervalDays);
        item.setRepetitions(result.repetitions);
        item.setNextReviewAt(result.nextReviewAt);
        item.setLastReviewedAt(LocalDateTime.now());

        studyItemRepository.save(item);
    }

    public static class ReviewResult {
        public double easeFactor;
        public int intervalDays;
        public int repetitions;
        public LocalDateTime nextReviewAt;

        public ReviewResult(double easeFactor, int intervalDays, int repetitions, LocalDateTime nextReviewAt) {
            this.easeFactor = easeFactor;
            this.intervalDays = intervalDays;
            this.repetitions = repetitions;
            this.nextReviewAt = nextReviewAt;
        }
    }

    /**
     * Calculates the next review schedule using the SuperMemo-2 (SM-2) algorithm.
     */
    public ReviewResult calculateNextReview(double currentEaseFactor, int currentInterval, int currentRepetitions,
            int quality) {
        double newEaseFactor = currentEaseFactor;
        int newInterval = currentInterval;
        int newRepetitions = currentRepetitions;

        if (quality < 3) {
            newRepetitions = 0;
            newInterval = 1;
        } else {
            // Update Ease Factor
            // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            newEaseFactor = currentEaseFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (newEaseFactor < 1.3) {
                newEaseFactor = 1.3;
            }

            newRepetitions++;

            if (newRepetitions == 1) {
                newInterval = 1;
            } else if (newRepetitions == 2) {
                newInterval = 6;
            } else {
                newInterval = (int) Math.round(newInterval * newEaseFactor);
            }
        }

        LocalDateTime nextReviewDate = LocalDateTime.now().plusDays(newInterval);

        return new ReviewResult(newEaseFactor, newInterval, newRepetitions, nextReviewDate);
    }
}
