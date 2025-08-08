package com.example.cs25service.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.cs25service.domain.userQuizAnswer.controller.UserQuizAnswerController.submitAnswer(..))")
    public void submitAnswer() {}

    @Pointcut("execution(* com.example.cs25service.domain.userQuizAnswer.controller.UserQuizAnswerController.evaluateAnswer(..))")
    public void evaluateAnswer() {}

    @Pointcut("submitAnswer() || evaluateAnswer()")
    public void quizAnswerMethods() {}

    @Around("quizAnswerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1) 호출 시간
        String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2) 사용자 정보
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : "anonymous");

        // 3) 퀴즈 정보
        Object[] args = joinPoint.getArgs();
        String quizInfo = (args.length > 0 && args[0] != null) ? args[0].toString() : "no-args";

        log.info("[{}] user = {} quizInfo = {}", time, username, quizInfo);

        return joinPoint.proceed();
    }
}
