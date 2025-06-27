package com.example.cs25batch.sender;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.JavaMailService;
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
}
