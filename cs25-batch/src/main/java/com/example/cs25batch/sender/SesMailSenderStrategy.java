package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.SesMailService;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component("sesMailSender")
public class SesMailSenderStrategy implements MailSenderStrategy{

    private final SesMailService sesMailService;
    private final Bucket bucket = Bucket.builder()
            .addLimit(limit ->
                    limit
                            .capacity(14)
                            .refillIntervally(7, Duration.ofMillis(500))
            )
            .build();

    @Override
    public void sendQuizMail(MailDto mailDto) {
        sesMailService.sendQuizEmail(mailDto.getSubscription(), mailDto.getQuiz());
    }

    @Override
    public Bucket getBucket() {
        return bucket;
    }
}
