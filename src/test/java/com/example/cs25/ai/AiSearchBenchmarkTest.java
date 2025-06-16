package com.example.cs25.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.cs25.domain.ai.service.RagService;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class AiSearchBenchmarkTest {

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
                ("249387ff-8136-4c87-a4a5-3b3effa2b2b8"), // Web-Spring-Spring MVC.txt
                ("8ced8aaa-b171-4bea-a75b-d209b2cfdaa5"),
                // Web-Spring-[Spring Boot] SpringApplication.txt
                ("b0465385-62c2-4483-9c7f-74eb77e53fab"), // Web-Spring-JPA.txt
                ("cfb8169c-600d-405e-adfd-4972b4f670f7"), // Web-Spring-[Spring Boot] Test Code.txt
                ("a5567f5a-6c1d-40da-af97-0ae262e680a5"), // Web-Spring-[Spring] Bean Scope.txt
                ("8e79a167-6909-4e10-a4d7-be87c07079c5"),
                // Web-Spring-[Spring Data JPA] 더티 체킹 (Dirty Checking).txt
                ("8dfffd84-247d-4d1e-abc3-0326c515d895")
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
        int[] topKs = {10, 20, 30};
        double[] thresholds = {0.5, 0.7, 0.9};

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
}