package com.example.cs25batch.util;

public class MailLinkGenerator {
    private static final String DOMAIN = "https://cs25.co.kr";

    public static String generateQuizLink(String subscriptionId, String quizId) {
        return String.format("%s/todayQuiz?subscriptionId=%s&quizId=%s", DOMAIN, subscriptionId, quizId);
    }

    public static String generateSubscriptionSettings(String subscriptionId) {
        return String.format("%s/subscriptions/%s", DOMAIN, subscriptionId);
    }
}
