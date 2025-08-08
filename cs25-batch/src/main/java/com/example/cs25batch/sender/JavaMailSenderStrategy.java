package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.JavaMailService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("javaBatchMailSender")
@RequiredArgsConstructor
public class JavaMailSenderStrategy implements MailSenderStrategy{
    private final JavaMailService javaMailService;
    private final Bucket bucket = Bucket.builder()
            .addLimit(
                    Bandwidth.builder()
                            .capacity(4)
                            .refillGreedy(2, Duration.ofMillis(500))
                            .build()
            )
            .build();

    @Override
    public void sendQuizMail(MailDto mailDto) {
        javaMailService.sendQuizEmail(mailDto.getSubscription(), mailDto.getQuiz());  // 커스텀 메서드로 정의
    }

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
