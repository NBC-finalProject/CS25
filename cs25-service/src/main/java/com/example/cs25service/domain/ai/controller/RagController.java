package com.example.cs25service.domain.ai.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.ai.service.RagService;
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

    // 전체 문서 조회
    @GetMapping("/documents")
    public ApiResponse<List<Document>> getAllDocuments() {
        List<Document> docs = ragService.getAllDocuments();
        return new ApiResponse<>(200, docs);
    }

    // 키워드로 문서 검색
    @GetMapping("/documents/search")
    public ApiResponse<List<Document>> searchDocuments(@RequestParam String keyword) {
        List<Document> docs = ragService.searchRelevant(keyword);
        return new ApiResponse<>(200, docs);
    }
}
