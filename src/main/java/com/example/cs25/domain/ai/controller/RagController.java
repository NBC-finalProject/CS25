package com.example.cs25.domain.ai.controller;

import com.example.cs25.domain.ai.service.RagService;
import com.example.cs25.global.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // 키워드로 문서 검색
    @GetMapping("/documents/search")
    public ApiResponse<List<Document>> searchDocuments(@RequestParam String keyword) {
        List<Document> docs = ragService.searchRelevant(keyword, 3, 0.1);
        return new ApiResponse<>(200,
            docs);
    }
}
