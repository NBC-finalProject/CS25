package com.example.cs25service.domain.mail.dto;

import com.example.cs25entity.domain.mail.entity.MailLog;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MailLogResponse {
    private Long mailLogId;
    private Long subscriptionId;
    private String email;
    private Long quizId;
    private LocalDateTime sendDate;
    private String status;

    public static MailLogResponse from(MailLog mailLog) {
        return MailLogResponse.builder()
            .mailLogId(mailLog.getId())
            .subscriptionId(mailLog.getSubscription().getId())
            .email(mailLog.getSubscription().getEmail())
            .quizId(mailLog.getQuiz().getId())
            .sendDate(mailLog.getSendDate())
            .status(mailLog.getStatus().name())
            .build();
    }
}
