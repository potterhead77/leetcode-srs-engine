package com.leetcoder.infrastructure.repository;

import com.leetcoder.domain.entity.StudyItem;
import com.leetcoder.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyItemRepository extends JpaRepository<StudyItem, Long> {
    Optional<StudyItem> findByUserAndQuestionTitleSlug(User user, String titleSlug);

    List<StudyItem> findAllByUser(User user);

    @Query("SELECT s FROM StudyItem s WHERE s.nextReviewAt <= :now")
    List<StudyItem> findAllDueItems(LocalDateTime now);

    List<StudyItem> findByNextReviewAtBefore(LocalDateTime now);
}
