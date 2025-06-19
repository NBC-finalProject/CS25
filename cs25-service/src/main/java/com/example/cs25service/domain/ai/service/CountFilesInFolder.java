package com.example.cs25service.domain.ai.service;

import java.io.File;

public class CountFilesInFolder {
    public static void main(String[] args) {
        // 폴더 경로 지정
        String folderPath = "data/markdowns";
        File folder = new File(folderPath);

        // 폴더가 존재하고, 디렉터리인지 확인
        if (folder.exists() && folder.isDirectory()) {
            // .txt 파일만 필터링
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                System.out.println("폴더 내 .txt 파일 개수: " + files.length);
                // 파일 이름도 출력 (선택)
                for (File file : files) {
                    System.out.println(file.getName());
                }
            } else {
                System.out.println("폴더 내 .txt 파일이 없습니다.");
            }
        } else {
            System.out.println("폴더가 존재하지 않거나, 디렉터리가 아닙니다.");
        }
    }
}

