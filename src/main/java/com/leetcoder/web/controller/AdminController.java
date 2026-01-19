package com.leetcoder.web.controller;

import com.leetcoder.application.service.LeetCodeSyncService;
import com.leetcoder.domain.entity.StudyItem;
import com.leetcoder.domain.entity.User;
import com.leetcoder.infrastructure.repository.StudyItemRepository;
import com.leetcoder.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LeetCodeSyncService syncService;
    private final com.leetcoder.application.service.DailyReminderService dailyReminderService;
    private final UserRepository userRepository;
    private final StudyItemRepository studyItemRepository;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/sync/{userId}")
    public ResponseEntity<String> syncUser(@PathVariable UUID userId) {
        System.out.println("DEBUG: Received Sync Request for UserId: '" + userId + "'");
        if (userId == null) {
            return ResponseEntity.badRequest().body("UserId cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        syncService.syncUser(user);
        return ResponseEntity.ok("Sync triggered for user: " + user.getLeetcodeUsername());
    }

    @PostMapping("/reset/{userId}")
    public ResponseEntity<String> resetProgress(@PathVariable UUID userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("UserId cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<StudyItem> items = studyItemRepository.findAllByUser(user);
        for (StudyItem item : items) {
            item.setEaseFactor(2.5);
            item.setIntervalDays(0);
            item.setRepetitions(0);
            item.setNextReviewAt(null);
            item.setLastReviewedAt(null);
            studyItemRepository.save(item);
        }

        return ResponseEntity.ok("Reset progress for " + items.size() + " items.");
    }

    @PostMapping("/reminders")
    public ResponseEntity<String> triggerReminders() {
        dailyReminderService.sendDailyReminders();
        return ResponseEntity.ok("Daily reminders triggered.");
    }
}
