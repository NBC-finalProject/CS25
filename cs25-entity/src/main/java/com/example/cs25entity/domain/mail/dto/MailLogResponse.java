package com.example.cs25entity.domain.mail.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MailLogResponse {

    private final Long mailLogId;
    private final Long subscriptionId;
    private final Long quizId;
    private final LocalDateTime sendDate;
    private final String mailStatus;
}
