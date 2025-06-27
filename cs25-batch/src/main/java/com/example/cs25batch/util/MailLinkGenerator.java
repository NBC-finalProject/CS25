package com.example.cs25batch.util;

public class MailLinkGenerator {
    private static final String DOMAIN = "https://cs25.co.kr/todayQuiz";

    public static String generateQuizLink(String subscriptionId, String quizId) {
        return String.format("%s?subscriptionId=%s&quizId=%s", DOMAIN, subscriptionId, quizId);
    }
}
