package com.example.cs25service.domain.mailSender;

import com.example.cs25service.domain.mail.service.SesMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("sesServiceMailSender")
public class SesMailSenderStrategy implements MailSenderServiceStrategy{

    private final SesMailService sesMailService;

    @Override
    public void sendVerificationCodeMail(String toEmail, String code) {
        sesMailService.sendVerificationCodeEmail(toEmail, code);
    }
}
