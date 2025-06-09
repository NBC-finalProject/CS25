package com.example.cs25.domain.mail.aop;

import com.example.cs25.domain.mail.entity.MailLog;
import com.example.cs25.domain.mail.enums.MailStatus;
import com.example.cs25.domain.mail.repository.MailLogRepository;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MailLogAspect {

    private final MailLogRepository mailLogRepository;

    @Around("execution(* com.example.cs25.domain.mail.service.MailService.sendQuizEmail(..))")
    public Object logMailSend(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        Subscription subscription = (Subscription) args[0];
        Quiz quiz = (Quiz) args[1];
        LocalDateTime sendTime = LocalDateTime.now();
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
                .sendDate(sendTime)
                .status(status)
                .build();

            mailLogRepository.save(log);
        }
    }
}
