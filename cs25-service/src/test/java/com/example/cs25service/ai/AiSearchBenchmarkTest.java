package com.example.cs25service.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.cs25service.domain.ai.service.AiFeedbackStreamWorker;
import com.example.cs25service.domain.ai.service.RagService;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.ai.document.Document;
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
public class AiSearchBenchmarkTest {

    @Autowired
    private AiFeedbackStreamWorker aiFeedbackStreamWorker;

    @Autowired
    private RagService ragService;
    @Autowired
    private VectorStore vectorStore;

    private List<String> testQueries;
    private Map<String, Set<String>> groundTruth;

    @BeforeEach
    public void setup() {
        // Spring 관련 쿼리 목록
        testQueries = List.of("Spring");

        // 저장 후 조회해서 파일명과 실제 id 매핑
//        List<Document> savedDocs = ragService.getAllDocuments();
//        Map<String, String> fileNameToId = new HashMap<>();
//        for (Document doc : savedDocs) {
//            String fileName = (String) doc.getMetadata().get("fileName");
//            if (fileName != null) {
//                fileNameToId.put(fileName, doc.getId());
//            }
//        }

        // 정답 문서 집합 (실제 파일명으로 지정)
        groundTruth = Map.of(
            "Spring", Set.of(
                "Web-Spring-Spring MVC.txt_chunk1",
                        "Web-Spring-Spring MVC.txt_chunk2", // Web-Spring-Spring MVC.txt
                "Web-Spring-[Spring Boot] SpringApplication.txt_chunk0",
                        "Web-Spring-[Spring Boot] SpringApplication.txt_chunk1", // Web-Spring-[Spring Boot] SpringApplication.txt
                "Web-Spring-JPA.txt_chunk0",
                        "Web-Spring-JPA.txt_chunk1",
                        "Web-Spring-JPA.txt_chunk2",
                        "Web-Spring-JPA.txt_chunk3",// Web-Spring-JPA.txt
                "Web-Spring-[Spring Boot] Test Code.txt_chunk0",
                        "Web-Spring-[Spring Boot] Test Code.txt_chunk1",
                        "Web-Spring-[Spring Boot] Test Code.txt_chunk2",// Web-Spring-[Spring Boot] Test Code.txt
                "Web-Spring-[Spring] Bean Scope.txt_chunk0",
                        "Web-Spring-[Spring] Bean Scope.txt_chunk2",
                        "Web-Spring-[Spring] Bean Scope.txt_chunk3",// Web-Spring-[Spring] Bean Scope.txt
                "Web-Spring-[Spring Data JPA] 더티 체킹 (Dirty Checking).txt_chunk2",
                        "Web-Spring-[Spring Data JPA] 더티 체킹 (Dirty Checking).txt_chunk3",
                        "Web-Spring-[Spring Data JPA] 더티 체킹 (Dirty Checking).txt_chunk4",
                // Web-Spring-[Spring Data JPA] 더티 체킹 (Dirty Checking).txt
                "Web-Spring-Spring Security - Authentication and Authorization.txt_chunk4",
                        "Web-Spring-Spring Security - Authentication and Authorization.txt_chunk1",
                        "Web-Spring-Spring Security - Authentication and Authorization.txt_chunk2",
                        "Web-Spring-Spring Security - Authentication and Authorization.txt_chunk3",
                        "Web-Spring-Spring Security - Authentication and Authorization.txt_chunk5"
                // Web-Spring-Spring Security - Authentication and Authorization.txt
            )
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
        int[] topKs = {5, 10, 20, 30};
        double[] thresholds = {0.1, 0.3, 0.5, 0.7, 0.9};

        try (PrintWriter writer = new PrintWriter("spring_benchmark_results.csv")) {
            writer.println("query,topK,threshold,result_count,elapsed_ms,precision,recall");
            for (String query : testQueries) {
                for (int topK : topKs) {
                    for (double threshold : thresholds) {
                        long start = System.currentTimeMillis();
                        List<Document> results = ragService.searchRelevant(query, topK, threshold);
                        long elapsed = System.currentTimeMillis() - start;

                        assertNotNull(results);

                        Set<String> retrieved = results.stream()
                            .map(Document::getId)
                            .collect(Collectors.toSet());
                        Set<String> truth = groundTruth.getOrDefault(query, Set.of());

                        // 디버깅용
                        System.out.println("retrieved: " + retrieved);
                        System.out.println("truth: " + truth);
                        System.out.println(
                            "교집합 개수: " + truth.stream().filter(retrieved::contains).count());

                        double precision = calculatePrecision(truth, retrieved);
                        double recall = calculateRecall(truth, retrieved);

                        writer.printf("%s,%d,%.2f,%d,%d,%.2f,%.2f%n",
                            query, topK, threshold, results.size(), elapsed, precision, recall);
                    }
                }
            }
        }
    }

    @AfterEach
    void tearDown() {
        aiFeedbackStreamWorker.stop();
    }
}
