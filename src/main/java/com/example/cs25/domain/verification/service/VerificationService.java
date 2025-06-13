package com.example.cs25.domain.verification.service;

import com.example.cs25.domain.mail.exception.CustomMailException;
import com.example.cs25.domain.mail.exception.MailExceptionCode;
import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.domain.verification.exception.VerificationException;
import com.example.cs25.domain.verification.exception.VerificationExceptionCode;
import jakarta.mail.MessagingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final String PREFIX = "VERIFY:";
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    private static final String ATTEMPT_PREFIX = "VERIFY_ATTEMPT:";
    private static final int MAX_ATTEMPTS = 5;

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
        save(email, verificationCode, Duration.ofMinutes(3));
        try {
            mailService.sendVerificationCodeEmail(email, verificationCode);
        }
        catch (MessagingException | MailException e) {
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
            redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts + 1), Duration.ofMinutes(10));
            throw new VerificationException(
                VerificationExceptionCode.VERIFICATION_CODE_EXPIRED_ERROR);
        }
        if (!stored.equals(code)) {
            redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts + 1), Duration.ofMinutes(10));
            throw new VerificationException(VerificationExceptionCode.VERIFICATION_CODE_MISMATCH_ERROR);
        }
        delete(email);
        redisTemplate.delete(attemptKey);
    }
}
