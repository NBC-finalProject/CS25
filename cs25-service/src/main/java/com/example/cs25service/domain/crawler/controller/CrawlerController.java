package com.example.cs25service.domain.crawler.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.crawler.dto.CreateDocumentRequest;
import com.example.cs25service.domain.crawler.service.CrawlerService;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlerService crawlerService;

    @PostMapping("/crawlers/github")
    public ApiResponse<String> crawlingGithub(
        @Valid @RequestBody CreateDocumentRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        try {
            crawlerService.crawlingGithubDocument(authUser, request.getLink());
            return new ApiResponse<>(200, request.getLink() + " 크롤링 성공");
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, "잘못된 GitHub URL: " + e.getMessage());
        } catch (Exception e) {
            return new ApiResponse<>(500, "크롤링 중 오류 발생: " + e.getMessage());
        }
    }
}
