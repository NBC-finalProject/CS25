package com.example.cs25service.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.cs25service.domain.ai.service.RagService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // 스프링 컨텍스트 리프레시
class RagServiceTest {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private RagService ragService;
    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

    @Test
    void insertDummyDocumentsAndSearch() {
        // given: 가상의 CS 문서 2개 삽입
        Document doc1 = new Document(
            "운영체제에서 프로세스와 스레드는 서로 다른 개념이다. 프로세스는 독립적인 실행 단위이고, 스레드는 프로세스 내의 작업 단위다.");
        Document doc2 = new Document(
            "TCP는 연결 기반의 프로토콜로, 패킷 손실 없이 순서대로 전달된다. UDP는 비연결 기반이며 빠르지만 신뢰성이 낮다.");

        vectorStore.add(List.of(doc1, doc2));

        // when: 키워드 기반으로 유사 문서 검색
        List<Document> result = vectorStore.similaritySearch("TCP, UDP");

        // then
        assertFalse(result.isEmpty());
        System.out.println("검색된 문서: " + result.get(0).getText());
    }

    @Test
    public void testEmbedWithSmallFiles() throws IOException {
        // 임시로 파일 2개만 남기고 테스트
        File folder = new File("data/markdowns");
        File[] originals = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (originals == null || originals.length <= 2) {
            // 이미 파일이 2개 이하라면 그대로 테스트
            ragService.saveMarkdownChunksToVectorStore();
        } else {
            // 파일 2개만 남기고 나머지는 임시로 이동
            File tempDir = new File("data/markdowns_temp");
            tempDir.mkdirs();
            for (int i = 2; i < originals.length; i++) {
                File original = originals[i];
                Files.move(original.toPath(), Path.of(tempDir.getPath(), original.getName()));
            }
            try {
                ragService.saveMarkdownChunksToVectorStore();
            } finally {
                // 테스트 후 원상복구
                File[] temps = tempDir.listFiles((dir, name) -> name.endsWith(".txt"));
                if (temps != null) {
                    for (File temp : temps) {
                        Files.move(temp.toPath(), Path.of(folder.getPath(), temp.getName()));
                    }
                }
                tempDir.delete();
            }
        }
    }

    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}

