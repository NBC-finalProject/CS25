package com.example.cs25common.global.domain.mail.aop;

import com.example.cs25common.global.domain.mail.entity.MailLog;
import com.example.cs25common.global.domain.mail.enums.MailStatus;
import com.example.cs25common.global.domain.mail.repository.MailLogRepository;
import com.example.cs25common.global.domain.quiz.entity.Quiz;
import com.example.cs25common.global.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MailLogAspect {

    private final MailLogRepository mailLogRepository;
    private final StringRedisTemplate redisTemplate;

    @Around("execution(* com.example.cs25common.global.domain.mail.service.MailService.sendQuizEmail(..))")
    public Object logMailSend(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Subscription subscription = (Subscription) args[0];
        Quiz quiz = (Quiz) args[1];
        MailStatus status = null;

        try {
            Object result = joinPoint.proceed(); // 메서드 실제 실행
            status = MailStatus.SENT;
            return result;
        } catch (Exception e) {
            status = MailStatus.FAILED;
            throw e;
        } finally {
            MailLog log = MailLog.builder()
                .subscription(subscription)
                .quiz(quiz)
                .sendDate(LocalDateTime.now())
                .status(status)
                .build();

            mailLogRepository.save(log);

            if (status == MailStatus.FAILED) {
                Map<String, String> retryMessage = Map.of(
                    "email", subscription.getEmail(),
                    "subscriptionId", subscription.getId().toString(),
                    "quizId", quiz.getId().toString()
                );
                redisTemplate.opsForStream().add("quiz-email-retry-stream", retryMessage);
            }
        }
    }
}
