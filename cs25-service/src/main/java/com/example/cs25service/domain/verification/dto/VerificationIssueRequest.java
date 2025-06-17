package com.example.cs25service.domain.verification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationIssueRequest(
    @NotBlank @Email String email
) {

}
