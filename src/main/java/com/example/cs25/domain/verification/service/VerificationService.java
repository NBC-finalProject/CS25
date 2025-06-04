package com.example.cs25.domain.verification.service;

import com.example.cs25.domain.verification.exception.VerificationException;
import com.example.cs25.domain.verification.exception.VerificationExceptionCode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "VERIFY:";

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

    public void issue(String email){
        String verificationCode = create();
        save(email, verificationCode, Duration.ofMinutes(3));
    }

    public boolean verify(String email, String inputCode) {
        String stored = get(email);
        if(stored == null){
            throw new VerificationException(VerificationExceptionCode.VERIFICATION_CODE_EXPIRED_ERROR);
        }
        if(!stored.equals(inputCode)){
            throw new VerificationException(VerificationExceptionCode.VERIFICATION_CODE_MISMATCH_ERROR);
        }
        delete(email);
        return true;
    }
}
