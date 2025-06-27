package com.example.cs25service.domain.mail.dto;

import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MailLogResponse {
    private Long mailLogId;
    private Long subscriptionId;
    private String email;
    private LocalDateTime sendDate;
    private String status;

    public static MailLogResponse from(MailLog mailLog) {
        return MailLogResponse.builder()
            .mailLogId(mailLog.getId())
            .subscriptionId(Optional.ofNullable(mailLog.getSubscription())
                .map(Subscription::getId)
                .orElse(null)) //회원이 탈퇴한 경우
            .email(mailLog.getSubscription().getEmail())
            .sendDate(mailLog.getSendDate())
            .status(mailLog.getStatus().name())
            .build();
    }
}
