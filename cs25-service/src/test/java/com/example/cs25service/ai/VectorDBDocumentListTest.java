package com.example.cs25service.ai;

import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)  // 스프링 컨텍스트 리프레시
@Disabled
public class VectorDBDocumentListTest {

    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void listAllDocuments() {
        // 저장된 모든 문서 조회 (topK를 충분히 크게 지정)
        List<Document> savedDocs = vectorStore.similaritySearch(SearchRequest.builder()
            .query("all")
            .topK(10000) // 충분히 큰 값으로 지정
            .build());

        // 각 문서의 id, 내용, 메타데이터 출력
        for (Document doc : savedDocs) {
            log.info("id: {}, fileName: {}, content: {}",
                doc.getId(),
                doc.getMetadata().get("fileName"),
                doc.getText().substring(0, Math.min(50, doc.getText().length())) + "..."
            );
        }
        log.info("총 문서 개수: {}", savedDocs.size());

        for (Document doc : savedDocs) {
            String content = doc.getText();
            log.info("fileName={}, containsSpring={}", doc.getMetadata().get("fileName"),
                content.contains("Spring"));
        }
    }
    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}
