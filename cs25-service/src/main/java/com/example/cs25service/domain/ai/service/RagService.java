package com.example.cs25service.domain.ai.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    public void saveMarkdownChunksToVectorStore() throws IOException {
        // 현재 작업 디렉터리와 폴더 절대 경로 출력
		log.info("현재 작업 디렉터리: {}", System.getProperty("user.dir"));
        File folder = new File("data/markdowns");
		log.info("폴더 절대 경로: {}", folder.getAbsolutePath());

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            log.error("폴더 또는 파일이 존재하지 않습니다.");
            return;
        }

        int totalChunks = 0;
        for (File file : files) {
            int chunkSize = 1000;
            int overlap = 100;
            int chunkIndex = 0;
            List<Document> docs = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                StringBuilder chunkBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    chunkBuilder.append(line).append("\n");
                    while (chunkBuilder.length() >= chunkSize) {
                        String chunk = chunkBuilder.substring(0, chunkSize);
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("fileName", file.getName());
                        metadata.put("chunkIndex", chunkIndex);
                        docs.add(new Document(file.getName() + "_chunk" + chunkIndex, chunk, metadata));
                        chunkIndex++;
                        if (docs.size() == 100) {
                            vectorStore.add(docs);
                            docs.clear();
                        }
                        // 오버랩 처리
                        chunkBuilder.delete(0, chunkSize - overlap);
                    }
                }
                // 남은 데이터 저장
                if (!chunkBuilder.isEmpty()) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("fileName", file.getName());
                    metadata.put("chunkIndex", chunkIndex);
                    docs.add(new Document(file.getName() + "_chunk" + chunkIndex, chunkBuilder.toString(), metadata));
                }
                if (!docs.isEmpty()) {
                    vectorStore.add(docs);
                }
            }
            totalChunks += chunkIndex + 1; // 마지막 청크 인덱스 + 1 (0부터 시작)
        }
        log.info("{}개 청크 저장 완료", totalChunks);
    }

    public List<Document> searchRelevant(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build()
        );
    }
}
