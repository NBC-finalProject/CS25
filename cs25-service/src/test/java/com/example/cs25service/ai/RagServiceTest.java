package com.example.cs25service.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RagServiceTest {

    @Autowired
    private VectorStore vectorStore;

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
}

