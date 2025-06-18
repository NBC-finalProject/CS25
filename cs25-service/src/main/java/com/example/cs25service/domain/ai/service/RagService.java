package com.example.cs25service.domain.ai.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

//    public void saveDocumentsToVectorStore(List<Document> docs) {
//        vectorStore.add(docs);
//        System.out.println(docs.size() + "개 문서 저장 완료");
//    }

    public void saveMarkdownChunksToVectorStore() throws IOException {
        // 1. 폴더 경로 지정
        File folder = new File("data/markdowns");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            log.error("폴더 또는 파일이 존재하지 않습니다.");
            return;
        }

        // 2. 청크 분할 및 Document 생성
        List<Document> allDocs = new ArrayList<>();
        for (File file : files) {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            List<String> chunks = splitByLength(text, 500, 100); // 청크 500자, 오버랩 100자
            for (int i = 0; i < chunks.size(); i++) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("fileName", file.getName());
                metadata.put("chunkIndex", i);
                allDocs.add(new Document(
                        file.getName() + "_chunk" + i, // id
                        chunks.get(i),                 // content
                        metadata                       // metadata
                ));
            }
        }

        // 3. 벡터DB에 저장
        vectorStore.add(allDocs);
        log.info("{}개 청크 저장 완료", allDocs.size());
    }

    /**
     * 텍스트를 청크와 오버랩으로 분할
     */
    private List<String> splitByLength(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    public List<Document> searchRelevant(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build());
    }
}
