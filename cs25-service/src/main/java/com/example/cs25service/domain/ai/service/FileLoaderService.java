package com.example.cs25service.domain.ai.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileLoaderService {

    private static final int MAX_CHUNK_SIZE = 2000; // 문자 기준. 토큰과 대략 1:1~1.3 비율

    private final VectorStore vectorStore;

    public void loadAndSaveFiles(String dirPath) {
        log.info("VectorStore 타입: {}", vectorStore.getClass().getName());

        try {
            List<Path> files = Files.list(Paths.get(dirPath))
                .filter(p -> p.toString().endsWith(".md") || p.toString().endsWith(".txt"))
                .toList();

            List<Document> documents = new ArrayList<>();

            for (Path file : files) {
                String content = Files.readString(file);
                List<Document> chunks = splitIntoChunks(content, MAX_CHUNK_SIZE, file);
                documents.addAll(chunks);
            }

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("{}개 문서 청크를 벡터DB에 저장했습니다.", documents.size());
            }
        } catch (IOException e) {
            log.error("파일 로드 실패: {}", e.getMessage());
        }
    }

    private List<Document> splitIntoChunks(String content, int chunkSize, Path file) {
        List<Document> chunks = new ArrayList<>();
        int length = content.length();

        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            String chunkText = content.substring(i, end);

            Document chunkDoc = new Document(
                chunkText,
                Map.of(
                    "fileName", file.getFileName().toString(),
                    "path", file.toString(),
                    "chunkIndex", String.valueOf(i / chunkSize),
                    "source", "local"
                )
            );
            chunks.add(chunkDoc);
        }

        return chunks;
    }
}