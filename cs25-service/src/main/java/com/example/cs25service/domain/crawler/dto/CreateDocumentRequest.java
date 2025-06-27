package com.example.cs25service.domain.crawler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class CreateDocumentRequest {
    @NotBlank(message = "Github repository 링크는 필수입니다.")
    private String link;
}
