package com.leetcoder.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "question_title_slug" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch is okay for reference data
    @JoinColumn(name = "question_title_slug", nullable = false)
    private Question question;

    // SM-2 Fields
    @Builder.Default
    private Double easeFactor = 2.5;

    @Builder.Default
    private Integer intervalDays = 0;

    @Builder.Default
    private Integer repetitions = 0;

    // Timing Fields
    private LocalDateTime lastReviewedAt;

    private LocalDateTime nextReviewAt;
}
