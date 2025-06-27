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
public class MailLogDetailResponse {
    private Long mailLogId;
    private Long subscriptionId;
    private String email;
    private Long quizId;
    private LocalDateTime sendDate;
    private String status;
    private String caused;

    public static MailLogDetailResponse from(MailLog mailLog) {
        return MailLogDetailResponse.builder()
            .mailLogId(mailLog.getId())
            .subscriptionId(Optional.ofNullable(mailLog.getSubscription())
                .map(Subscription::getId)
                .orElse(null)) //회원이 탈퇴한 경우
            .email(mailLog.getSubscription().getEmail())
            .quizId(Optional.ofNullable(mailLog.getQuiz())
                .map(Quiz::getId)
                .orElse(null)) //문제가 삭제된 경우
            .sendDate(mailLog.getSendDate())
            .status(mailLog.getStatus().name())
            .caused(mailLog.getCaused())
            .build();
    }
}
