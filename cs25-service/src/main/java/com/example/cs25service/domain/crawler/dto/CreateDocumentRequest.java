package com.example.cs25service.domain.crawler.dto;

import jakarta.validation.constraints.NotBlank;


public record CreateDocumentRequest(
    @NotBlank String link
) {

}
