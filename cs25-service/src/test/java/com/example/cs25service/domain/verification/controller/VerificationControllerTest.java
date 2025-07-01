package com.example.cs25service.domain.verification.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.verification.dto.VerificationIssueRequest;
import com.example.cs25service.domain.verification.dto.VerificationVerifyRequest;
import com.example.cs25service.domain.verification.service.VerificationPreprocessingService;
import com.example.cs25service.domain.verification.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(VerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // DB 설정이 그대로 사용됨 (application-test.properties 기반)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationService verificationService;

    @MockitoBean
    private VerificationPreprocessingService preprocessingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("POST /emails/verifications")
    class IssueVerificationCode {

        @Test
        @DisplayName("이메일 인증 코드 발급 요청에 성공하면 200 OK와 메시지를 반환한다")
        void issueVerificationCode_success() throws Exception {
            // given
            VerificationIssueRequest request = new VerificationIssueRequest("test@example.com");
            AuthUser authUser = null;

            // when
            doNothing().when(preprocessingService).isValidEmailCheck(anyString(), authUser);
            doNothing().when(verificationService).issue(anyString());

            // then
            mockMvc.perform(post("/emails/verifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("인증코드가 발급되었습니다."));
        }
    }

    @Nested
    @DisplayName("POST /emails/verifications/verify")
    class VerifyVerificationCode {

        @Test
        @DisplayName("이메일 인증 코드 검증 요청에 성공하면 200 OK와 메시지를 반환한다")
        void verifyVerificationCode_success() throws Exception {
            // given
            VerificationVerifyRequest request = new VerificationVerifyRequest("test@example.com",
                "123456");

            // when
            doNothing().when(verificationService).verify(anyString(), anyString());

            // then
            mockMvc.perform(post("/emails/verifications/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("인증 성공"));
        }
    }
}
