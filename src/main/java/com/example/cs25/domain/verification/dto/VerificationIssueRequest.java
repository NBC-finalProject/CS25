package com.example.cs25.domain.verification.dto;

import jakarta.validation.constraints.Email;

public record VerificationIssueRequest(
    @Email String email
) {

}
