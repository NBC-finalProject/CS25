package com.example.cs25batch.batch.component.writer;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.SesMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailWriter implements ItemWriter<MailDto> {

    private final SesMailService mailService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void write(Chunk<? extends MailDto> items) throws Exception {
        for (MailDto mail : items) {
            try {
                //long start = System.currentTimeMillis();
                mailService.sendQuizEmail(mail.getSubscription(), mail.getQuiz());
                //long end = System.currentTimeMillis();
                //log.info("[6. 메일 발송] email : {}ms", end - start);
            } catch (Exception e) {
                // 에러 로깅 또는 알림 처리
                System.err.println("메일 발송 실패: " + e.getMessage());
            } finally {
                try {
                    RecordId recordId = RecordId.of(mail.getRecordId());
                    redisTemplate.opsForStream().delete("quiz-email-stream", recordId);
                } catch (Exception e) {
                    log.warn("Redis 스트림 레코드 삭제 실패: recordId = {}, error = {}",
                        mail.getRecordId(), e.getMessage());
                }
            }
        }
    }
}
