package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.SesMailService;
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
}
