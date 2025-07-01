package com.example.cs25service.domain.mailSender;

public interface MailSenderServiceStrategy {
    void sendVerificationCodeMail(String email, String code);
}
