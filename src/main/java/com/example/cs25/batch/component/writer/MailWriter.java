package com.example.cs25.batch.component.writer;

import com.example.cs25.domain.mail.dto.MailDto;
import com.example.cs25.domain.mail.service.MailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailWriter implements ItemWriter<MailDto> {

    private final MailService mailService;

    @Override
    public void write(Chunk<? extends MailDto> items) throws Exception {
        for (MailDto mail : items) {
            try {
                //long start = System.currentTimeMillis();
                mailService.sendQuizEmail(mail.subscription(), mail.quiz());
                //long end = System.currentTimeMillis();
                //log.info("[6. 메일 발송] email : {}ms", end - start);

            } catch (Exception e) {
                // 에러 로깅 또는 알림 처리
                System.err.println("메일 발송 실패: " + e.getMessage());
            }
        }
    }
}
