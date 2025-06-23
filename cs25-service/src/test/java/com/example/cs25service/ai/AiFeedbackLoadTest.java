package com.example.cs25service.ai;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AiFeedbackLoadTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    private final int CONCURRENT_USERS = 50; // 동시 접속 사용자 수

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testConcurrentSseFeedbackRequests() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    long answerId = 1L + (userIndex % 5); // 다양한 answerId로 요청
                    String url = baseUrl + "/quizzes/" + answerId + "/feedback/stream";
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "text/event-stream");
                    connection.setReadTimeout(60000);
                    connection.setDoInput(true);

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                System.out.println("User-" + userIndex + " ▶ " + line);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("User-" + userIndex + " ❌ 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 요청 대기
        executor.shutdown();
    }
}

