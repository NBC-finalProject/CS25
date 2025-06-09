package com.example.cs25.global.crawler.controller;

import com.example.cs25.global.crawler.dto.CreateDocumentRequest;
import com.example.cs25.global.crawler.service.CrawlerService;
import com.example.cs25.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    @PostMapping("/crawlers/github")
    public ApiResponse<String> crawlingGithub(
        @Valid @RequestBody CreateDocumentRequest request
    ) {
        crawlerService.crawlingGithubDocument(request.link());
        return new ApiResponse<>(200, request.link() + " 크롤링 성공");
    }
}
