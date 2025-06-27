package com.example.cs25batch.aop;

import com.example.cs25entity.domain.mail.entity.MailLog;
import com.example.cs25entity.domain.mail.enums.MailStatus;
import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MailLogAspect {

    private final MailLogRepository mailLogRepository;
    private final StringRedisTemplate redisTemplate;

    @Around("execution(* com.example.cs25batch.batch.service.BatchMailService.sendQuizEmail(..))")
    public Object logMailSend(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Subscription subscription = (Subscription) args[0];
        Quiz quiz = (Quiz) args[1];
        MailStatus status = null;
        String caused = null;
        try {
            Object result = joinPoint.proceed(); // 메서드 실제 실행
            status = MailStatus.SENT;
            return result;
        } catch (SesV2Exception e) {
            status = MailStatus.FAILED;
            caused = e.awsErrorDetails().errorMessage();
            log.warn("발송 실패 원인 : {}", caused);
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        } catch (Exception e){
            status = MailStatus.FAILED;
            caused = e.getMessage();
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        } finally {
            MailLog log = MailLog.builder()
                .subscription(subscription)
                .quiz(quiz)
                .sendDate(LocalDateTime.now())
                .status(status)
                .caused(caused)
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