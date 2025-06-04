package com.example.cs25.domain.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationVerifyRequest(
    @Email String email,
    @NotBlank String code
) {

}
