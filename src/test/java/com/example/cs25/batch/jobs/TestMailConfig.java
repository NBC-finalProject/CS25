package com.example.cs25.batch.jobs;

import com.example.cs25.domain.mail.service.MailService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMailConfig {

    @Bean
    public MailService mailService() {
        return Mockito.mock(MailService.class);
    }
}