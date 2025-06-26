package com.example.cs25batch.batch.component.writer;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.BatchMailService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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

    private final BatchMailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final RateLimiter rateLimiter;

    @Override
    public void write(Chunk<? extends MailDto> items) throws Exception {
        for (MailDto mail : items) {
            try {
                //long start = System.currentTimeMillis();
                //mailService.sendQuizEmail(mail.getSubscription(), mail.getQuiz());
                Runnable sendEmail = RateLimiter
                    .decorateRunnable(rateLimiter, () -> mailService.sendQuizEmail(mail.getSubscription(), mail.getQuiz()));
                //long end = System.currentTimeMillis();
                //log.info("[6. 메일 발송] email : {}ms", end - start);
                try {
                    sendEmail.run(); // permit 없으면 예외 발생
                } catch (RequestNotPermitted ex) {
                    log.warn("메일 전송 속도 제한 - 요청 차단됨");
                }
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
