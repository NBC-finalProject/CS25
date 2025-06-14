package com.example.cs25.batch.component.writer;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.mail.service.MailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailWriter implements ItemWriter<MailDto> {

    private final MailService mailService;

    @Override
    public void write(Chunk<? extends MailDto> items) throws Exception {
        for (MailDto mail : items) {
            try {
                mailService.sendQuizEmail(mail.subscription(), mail.quiz());
            } catch (Exception e) {
                // 에러 로깅 또는 알림 처리
                System.err.println("메일 발송 실패: " + e.getMessage());
            }
        }
    }
}
