package com.example.cs25.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.cs25.domain.ai.service.RagService;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class AiSearchBenchmarkTest {

    @Autowired
    private RagService ragService;

    private List<String> testQueries;
    private Map<String, Set<String>> groundTruth;

    @BeforeEach
    public void setup() {
        // 테스트용 쿼리 목록
        testQueries = List.of("네트워크", "알고리즘", "암호키");

        // 테스트 문서 생성
        List<Document> testDocs = List.of(
            new Document("네트워크는 컴퓨터 간의 통신을 가능하게 하는 기술이다.", Map.of("id", "doc1")),
            new Document("알고리즘은 문제를 해결하는 절차 또는 방법이다.", Map.of("id", "doc2")),
            new Document("암호키는 데이터를 암호화/복호화하는 데 사용된다.", Map.of("id", "doc3"))
        );

        // 저장 전 문서 로그 및 유효성 검증
        List<Document> validDocs = testDocs.stream()
            .filter(doc -> {
                boolean isValid = doc.getText() != null && !doc.getText().isEmpty();
                if (!isValid) {
                    log.warn("유효하지 않은 문서 발견: content={}, metadata={}", doc.getText(),
                        doc.getMetadata());
                }
                return isValid;
            })
            .collect(Collectors.toList());

        log.info("유효한 테스트 문서 개수: {}", validDocs.size());

        ragService.saveDocumentsToVectorStore(validDocs);

        // 저장 후 실제로 조회해서 로그로 출력
        List<Document> savedDocs = ragService.getAllDocuments();
        log.info("저장 후 조회된 문서 개수: {}", savedDocs.size());
        savedDocs.forEach(doc -> {
            log.info("조회된 Content: {}", doc.getText());
            log.info("조회된 Metadata: {}", doc.getMetadata());
        });

        // 저장된 문서의 실제 ID를 확인해서 groundTruth에 넣기
        Map<String, String> docIdByContent = new HashMap<>();
        for (Document doc : savedDocs) {
            String id = (String) doc.getMetadata().get("id");
            if (id != null) {
                docIdByContent.put(id, doc.getId());
            }
        }

        // 정답 문서 집합 (실제 데이터셋에 맞춰 수정 필요)
        groundTruth = Map.of(
            "네트워크", Set.of("doc1"),
            "알고리즘", Set.of("doc2"),
            "암호키", Set.of("doc3")
        );
    }

    private double calculatePrecision(Set<String> groundTruth, Set<String> retrieved) {
        if (retrieved.isEmpty()) {
            return 0.0;
        }
        int truePositive = (int) groundTruth.stream().filter(retrieved::contains).count();
        return (double) truePositive / retrieved.size();
    }

    private double calculateRecall(Set<String> groundTruth, Set<String> retrieved) {
        if (groundTruth.isEmpty()) {
            return 0.0;
        }
        int truePositive = (int) groundTruth.stream().filter(retrieved::contains).count();
        return (double) truePositive / groundTruth.size();
    }

    @Test
    public void benchmarkSearch() throws Exception {
        int[] topKs = {3, 5, 10};
        double[] thresholds = {0.5, 0.7, 0.9};

        try (PrintWriter writer = new PrintWriter("benchmark_results.csv")) {
            writer.println("query,topK,threshold,result_count,elapsed_ms,precision,recall");
            for (String query : testQueries) {
                for (int topK : topKs) {
                    for (double threshold : thresholds) {
                        long start = System.currentTimeMillis();
                        List<Document> results = ragService.searchRelevant(query);
                        long elapsed = System.currentTimeMillis() - start;

                        assertNotNull(results);

                        Set<String> retrieved = results.stream()
                            .map(Document::getId)
                            .collect(Collectors.toSet());
                        Set<String> truth = groundTruth.getOrDefault(query, Set.of());

                        double precision = calculatePrecision(truth, retrieved);
                        double recall = calculateRecall(truth, retrieved);

                        writer.printf("%s,%d,%.2f,%d,%d,%.2f,%.2f%n",
                            query, topK, threshold, results.size(), elapsed, precision, recall);
                    }
                }
            }
        }
    }
}
