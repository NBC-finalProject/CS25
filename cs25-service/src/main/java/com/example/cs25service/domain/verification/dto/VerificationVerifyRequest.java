package com.example.cs25service.domain.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificationVerifyRequest(
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "\\d{6}") String code
) {

}
