package com.leetcoder.application.service;

import com.leetcoder.domain.entity.StudyItem;
import com.leetcoder.domain.entity.User;
import com.leetcoder.infrastructure.repository.StudyItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReminderService {

    private final StudyItemRepository studyItemRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Daily at 09:00 AM server time (simplification: not handling user timezones
    // dynamically yet)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        log.info("Starting Daily Reminder Job...");
        LocalDateTime now = LocalDateTime.now();
        List<StudyItem> dueItems = studyItemRepository.findAllDueItems(now);

        if (dueItems.isEmpty()) {
            log.info("No items due for review today.");
            return;
        }

        Map<User, List<StudyItem>> itemsByUser = dueItems.stream()
                .collect(Collectors.groupingBy(StudyItem::getUser));

        for (Map.Entry<User, List<StudyItem>> entry : itemsByUser.entrySet()) {
            User user = entry.getKey();
            List<StudyItem> items = entry.getValue();
            try {
                sendEmail(user, items);
            } catch (Exception e) {
                log.error("Failed to send email to {}", user.getEmail(), e);
            }
        }
    }

    private void sendEmail(User user, List<StudyItem> items) throws MessagingException {
        String subject = "Time to Code: " + items.size() + " Problems Due Today";

        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>Hello, ").append(user.getLeetcodeUsername()).append("!</h2>");
        body.append("<p>You have <strong>").append(items.size()).append("</strong> problems due for review today:</p>");
        body.append("<ul>");

        for (StudyItem item : items) {
            String title = item.getQuestion().getTitle();
            String url = item.getQuestion().getUrl();
            body.append("<li><a href=\"").append(url).append("\">").append(title).append("</a></li>");
        }

        body.append("</ul>");
        body.append("<p>Happy Coding!</p>");
        body.append("</body></html>");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        if (fromEmail != null) {
            helper.setFrom(fromEmail);
        }
        if (user.getEmail() != null) {
            String email = user.getEmail();
            if (email != null)
                helper.setTo(email);
        }
        helper.setSubject(subject);
        String text = body.toString();
        if (text != null)
            helper.setText(text, true); // true = html

        mailSender.send(message);
        log.info("Sent reminder email to {} with {} items.", user.getEmail(), items.size());
    }
}
