package com.example.cs25.global.crawler.dto;

import jakarta.validation.constraints.NotBlank;


public record CreateDocumentRequest(
    @NotBlank String link
) {

}
