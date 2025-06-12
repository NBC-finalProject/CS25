package com.example.cs25.domain.mail.dto;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.subscription.entity.Subscription;

public record MailDto(
    Subscription subscription,
    Quiz quiz
) {

}
