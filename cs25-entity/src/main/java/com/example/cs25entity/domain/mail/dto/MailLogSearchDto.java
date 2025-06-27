package com.example.cs25entity.domain.mail.dto;

import com.example.cs25entity.domain.mail.enums.MailStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MailLogSearchDto {
    private MailStatus mailStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}
