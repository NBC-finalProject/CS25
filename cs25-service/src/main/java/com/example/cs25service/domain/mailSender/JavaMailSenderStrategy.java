package com.example.cs25service.domain.mailSender;

import com.example.cs25service.domain.mail.service.JavaMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("javaServiceMailSender")
@RequiredArgsConstructor
public class JavaMailSenderStrategy implements MailSenderServiceStrategy{
    private final JavaMailService javaMailService;

    @Override
    public void sendVerificationCodeMail(String email, String code) {
        javaMailService.sendVerificationCodeEmail(email, code);
    }
}
