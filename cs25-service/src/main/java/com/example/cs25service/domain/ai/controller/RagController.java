package com.example.cs25service.domain.ai.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.ai.service.RagService;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;
    private final VectorStore vectorStore;

    // 키워드로 문서 검색
    @GetMapping("/documents/search")
    public ApiResponse<List<Document>> searchDocuments(@RequestParam String keyword) {
        List<Document> docs = ragService.searchRelevant(keyword, 3, 0.1);
        return new ApiResponse<>(200, docs);
    }

    // 벡터DB 전체 삭제
    @PostMapping("vector/delete-all")
    public String deleteAll() {
        // 1. 모든 문서 조회 (topK를 충분히 크게)
        List<Document> allDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("all")
                        .topK(10000) // 충분히 큰 값으로 전체 문서 조회
                        .build()
        );
        List<String> allIds = allDocs.stream().map(Document::getId).toList();

        // 2. id 리스트로 일괄 삭제
        vectorStore.delete(allIds);
        return "벡터DB 전체 삭제 완료";
    }


    // data/markdowns의 txt 파일 임베딩
    @PostMapping("vector/embed")
    public String embed() {
        try {
            ragService.saveMarkdownChunksToVectorStore();
            return "data/markdowns 임베딩 완료";
        } catch (IOException e) {
            return "임베딩 중 오류: " + e.getMessage();
        }
    }
}
