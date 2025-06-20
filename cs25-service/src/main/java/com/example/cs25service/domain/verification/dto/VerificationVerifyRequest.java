package com.example.cs25service.domain.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationVerifyRequest {

    @NotBlank(message = "이메일은 필수 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "인증코드는 필수 입니다.")
    @Pattern(regexp = "\\d{6}", message = "인증코드는 6자리의 숫자여야 합니다.")
    private String code;
}
