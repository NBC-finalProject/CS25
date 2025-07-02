package com.example.cs25service.domain.verification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.mail.exception.CustomMailException;
import com.example.cs25service.domain.mail.service.JavaMailService;
import com.example.cs25service.domain.mailSender.context.MailSenderServiceContext;
import com.example.cs25service.domain.verification.exception.VerificationException;
import jakarta.mail.MessagingException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MailSenderServiceContext mailSenderServiceContext;

    @InjectMocks
    private VerificationService verificationService;

    String email = "test@example.com";

    @Nested
    @DisplayName("issue 메서드는")
    class Issue {

        @BeforeEach
        void setupIssue() {
            // save() 내부의 opsForValue().set(...) 방어용
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            //when(valueOperations.get("VERIFY:" + email)).thenReturn("123456");
            ReflectionTestUtils.setField(verificationService, "strategy", "javaServiceMailSender");
        }

        @Test
        @DisplayName("정상적으로 인증 코드를 생성하고 이메일을 발송한다")
        void issueSuccess() throws MessagingException {
            // given
            doNothing().when(mailSenderServiceContext).send(anyString(), anyString(), anyString());

            // when & then
            assertDoesNotThrow(() -> verificationService.issue(email));
        }

        @Test
        @DisplayName("이메일 발송에 실패하면 인증 코드도 삭제되고 예외가 발생한다")
        void issueFailsAndCodeDeleted() throws MessagingException {
            // given
            doThrow(new MailException("실패") {
            }).when(mailSenderServiceContext).send(eq(email), anyString(), eq("javaServiceMailSender"));
            when(redisTemplate.delete("VERIFY:" + email)).thenReturn(true);

            // when & then
            assertThrows(CustomMailException.class, () -> verificationService.issue(email));
            verify(redisTemplate).delete("VERIFY:" + email);
        }
    }

    @Nested
    @DisplayName("verify 메서드는")
    class Verify {

        @BeforeEach
        void setupVerify() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("저장된 코드와 입력된 코드가 일치하면 인증에 성공하고 레디스에서 삭제한다")
        void verifySuccess() {
            String code = "123456";

            when(valueOperations.get("VERIFY:" + email)).thenReturn(code);
            when(valueOperations.get("VERIFY_ATTEMPT:" + email)).thenReturn(null);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            assertDoesNotThrow(() -> verificationService.verify(email, code));

            verify(redisTemplate).delete("VERIFY:" + email);
            verify(redisTemplate).delete("VERIFY_ATTEMPT:" + email);
        }

        @Test
        @DisplayName("인증 코드가 저장되어 있지 않으면 예외를 던지고 시도 횟수를 증가시킨다")
        void codeExpired() {
            when(valueOperations.get("VERIFY:" + email)).thenReturn(null);
            when(valueOperations.get("VERIFY_ATTEMPT:" + email)).thenReturn("2");

            assertThrows(VerificationException.class,
                () -> verificationService.verify(email, "999999"));

            verify(valueOperations).set("VERIFY_ATTEMPT:" + email, "3", Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("인증 코드가 일치하지 않으면 예외를 던지고 시도 횟수를 증가시킨다")
        void codeMismatch() {

            when(valueOperations.get("VERIFY:" + email)).thenReturn("123456");
            when(valueOperations.get("VERIFY_ATTEMPT:" + email)).thenReturn("1");

            assertThrows(VerificationException.class,
                () -> verificationService.verify(email, "000000"));

            verify(valueOperations).set("VERIFY_ATTEMPT:" + email, "2", Duration.ofMinutes(10));
        }

        @Test
        @DisplayName("인증 시도 횟수가 초과되면 TOO_MANY_ATTEMPTS 예외를 던진다")
        void tooManyAttempts() {
            when(valueOperations.get("VERIFY_ATTEMPT:" + email)).thenReturn("5");

            assertThrows(VerificationException.class,
                () -> verificationService.verify(email, "any"));
        }
    }

}
