package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.SesMailService;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("sesMailSender")
public class SesMailSenderStrategy implements MailSenderStrategy{

    private final SesMailService sesMailService;

    @Override
    public void sendQuizMail(MailDto mailDto) {
        sesMailService.sendQuizEmail(mailDto.getSubscription(), mailDto.getQuiz());
    }

    @Override
    public Bucket getRateLimiter() {
        return Bucket.builder()
            .addLimit(limit ->
                limit
                    .capacity(14)
                    .refillIntervally(7, Duration.ofMillis(500))
            )
            .build();
    }
}
