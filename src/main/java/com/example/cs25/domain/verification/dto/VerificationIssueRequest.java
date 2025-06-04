package com.example.cs25.domain.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationIssueRequest(
    @NotBlank @Email String email
) {

}
