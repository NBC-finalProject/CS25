package com.example.cs25service.domain.verification.service;


import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25entity.domain.mail.exception.MailExceptionCode;
import com.example.cs25service.domain.mail.service.JavaMailService;
import com.example.cs25service.domain.mailSender.context.MailSenderServiceContext;
import com.example.cs25service.domain.verification.exception.VerificationException;
import com.example.cs25service.domain.verification.exception.VerificationExceptionCode;
import jakarta.mail.MessagingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final String PREFIX = "VERIFY:";
    private static final String LIMITFIX = "VERIFY_DAILY_LIMIT:";
    private final StringRedisTemplate redisTemplate;
    private final MailSenderServiceContext mailSenderContext;

    private static final int MAX_ISSUE_ATTEMPTS = 3;

    private static final String ATTEMPT_PREFIX = "VERIFY_ATTEMPT:";
    private static final int MAX_ATTEMPTS = 5;

    @Value("${mail.strategy:javaServiceMailSender}")
    private String strategy;

    private String create() {
        int length = 6;
        Random random;

        try {
            random = SecureRandom.getInstanceStrong();
        } catch (
            NoSuchAlgorithmException e) { //SecureRandom.getInstanceStrong()에서 사용하는 알고리즘을 JVM 에서 지원하지 않을 때
            random = new SecureRandom();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }

    private void save(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + email, code, ttl);
    }

    private String get(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    private void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    public void issue(String email) {
        String verificationCode = create();
        redisTemplate.opsForValue().set(PREFIX + email, verificationCode, Duration.ofMinutes(3));

        Long count = redisTemplate.opsForValue().increment(LIMITFIX + email);
        if (count == 1) {
            redisTemplate.expire(LIMITFIX + email, Duration.ofDays(1));
        }
        else if (count > 3) {
            throw new VerificationException(VerificationExceptionCode.TOO_MANY_REQUESTS_DAILY);
        }

        redisTemplate.opsForValue().set(PREFIX + email, verificationCode, Duration.ofMinutes(3));

        try {
            mailSenderContext.send(email, verificationCode, strategy);
        } catch (Exception e) {
            delete(email);
            throw new CustomMailException(MailExceptionCode.EMAIL_SEND_FAILED_ERROR);
        }
    }

    public void verify(String email, String code) {
        String attemptKey = ATTEMPT_PREFIX + email;
        String attemptCount = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptCount != null ? Integer.parseInt(attemptCount) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            throw new VerificationException(VerificationExceptionCode.TOO_MANY_ATTEMPTS_ERROR);
        }
        String stored = get(email);
        if (stored == null) {
            redisTemplate.opsForValue()
                .set(attemptKey, String.valueOf(attempts + 1), Duration.ofMinutes(10));
            throw new VerificationException(
                VerificationExceptionCode.VERIFICATION_CODE_EXPIRED_ERROR);
        }
        if (!stored.equals(code)) {
            redisTemplate.opsForValue()
                .set(attemptKey, String.valueOf(attempts + 1), Duration.ofMinutes(10));
            throw new VerificationException(
                VerificationExceptionCode.VERIFICATION_CODE_MISMATCH_ERROR);
        }
        delete(email);
        redisTemplate.delete(attemptKey);
    }
}
