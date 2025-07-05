package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.JavaMailService;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("javaBatchMailSender")
@RequiredArgsConstructor
public class JavaMailSenderStrategy implements MailSenderStrategy{
    private final JavaMailService javaMailService;

    @Override
    public void sendQuizMail(MailDto mailDto) {
        javaMailService.sendQuizEmail(mailDto.getSubscription(), mailDto.getQuiz());  // 커스텀 메서드로 정의
    }

    @Override
    public Bucket getRateLimiter() {
        return Bucket.builder()
            .addLimit(limit ->
                limit
                    .capacity(4)
                    .refillIntervally(4, Duration.ofMillis(1000))
            )
            .build();
    }
}
