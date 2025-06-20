package com.example.cs25service.domain.verification.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.verification.dto.VerificationIssueRequest;
import com.example.cs25service.domain.verification.dto.VerificationVerifyRequest;
import com.example.cs25service.domain.verification.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emails/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping()
    public ApiResponse<String> issueVerificationCodeByEmail(
        @Valid @RequestBody VerificationIssueRequest request) {
        verificationService.issue(request.getEmail());
        return new ApiResponse<>(200, "인증코드가 발급되었습니다.");
    }

    @PostMapping("/verify")
    public ApiResponse<String> verifyVerificationCode(
        @Valid @RequestBody VerificationVerifyRequest request) {
        verificationService.verify(request.getEmail(), request.getCode());
        return new ApiResponse<>(200, "인증 성공");
    }
}
