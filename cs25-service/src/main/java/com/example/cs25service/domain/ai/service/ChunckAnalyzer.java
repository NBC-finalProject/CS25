package com.example.cs25service.domain.ai.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//문서 개수, 평균 청크 개수, 평균 파일 크기를 알아보자.
public class ChunckAnalyzer {
    public static void main(String[] args) throws IOException {
        File folder = new File("data/markdowns");
        List<Long> fileSizes = new ArrayList<>();
        List<Integer> chunkCounts = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    long size = file.length();
                    fileSizes.add(size);

                    int chunkSize = 500;
                    int overlap = 50;
                    int chunkCount = 0;
                    StringBuilder chunkBuilder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            chunkBuilder.append(line).append("\n");
                            while (chunkBuilder.length() >= chunkSize) {
                                chunkCount++;
                                chunkBuilder.delete(0, chunkSize - overlap);
                            }
                        }
                        // 남은 데이터 처리
                        if (!chunkBuilder.isEmpty()) {
                            chunkCount++;
                        }
                    }
                    chunkCounts.add(chunkCount);
                }
            }
        }

        // 평균 계산
        double avgFileSize = fileSizes.isEmpty() ? 0 :
                fileSizes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgChunkCount = chunkCounts.isEmpty() ? 0 :
                chunkCounts.stream().mapToInt(Integer::intValue).average().orElse(0);

        System.out.println("문서 개수: " + fileSizes.size());
        System.out.printf("평균 파일 크기: %.2f 바이트\n", avgFileSize);
        System.out.printf("평균 청크 개수: %.2f\n", avgChunkCount);
    }
}
