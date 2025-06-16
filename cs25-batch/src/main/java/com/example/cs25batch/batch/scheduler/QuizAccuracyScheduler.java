package com.example.cs25batch.batch.scheduler;

import com.example.cs25batch.batch.service.TodayQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizAccuracyScheduler {

    private final TodayQuizService quizService;

    @Scheduled(cron = "0 55 5 * * *")
    public void calculateAndCacheAllQuizAccuracies() {
        try {
            log.info("⏰ [Scheduler] 정답률 계산 시작");
            quizService.calculateAndCacheAllQuizAccuracies();
            log.info("[Scheduler] 정답률 계산 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 정답률 계산 중 오류 발생", e);
        }
    }
}
