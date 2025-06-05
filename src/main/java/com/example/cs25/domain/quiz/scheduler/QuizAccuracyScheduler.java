package com.example.cs25.domain.quiz.scheduler;

import com.example.cs25.domain.quiz.service.TodayQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizAccuracyScheduler {

    private final TodayQuizService quizService;

    @Scheduled(cron = "0 55 8 * * *")
    public void calculateAndCacheAllQuizAccuracies() {
        log.info("⏰ [Scheduler] 정답률 계산 시작");
        quizService.calculateAndCacheAllQuizAccuracies();
    }
}
