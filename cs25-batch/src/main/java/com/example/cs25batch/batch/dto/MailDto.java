package com.example.cs25batch.batch.dto;


import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MailDto {
    Subscription subscription;
    Quiz quiz;
}
