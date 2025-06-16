package com.example.cs25common.global.domain.mail.dto;


import com.example.cs25common.global.domain.quiz.entity.Quiz;
import com.example.cs25common.global.domain.subscription.entity.Subscription;

public record MailDto(
    Subscription subscription,
    Quiz quiz
) {

}
