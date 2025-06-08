package com.example.cs25.global.crawler.service;

import com.example.cs25.global.crawler.github.GitHubRepoInfo;
import com.example.cs25.global.crawler.github.GitHubUrlParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class CrawlerService {

    private final RestTemplate restTemplate;
    private String githubToken;

    public void crawlingGithubDocument(String url){
        //url에서 필요 정보 추출
        GitHubRepoInfo repoInfo = GitHubUrlParser.parseGitHubUrl(url);

        githubToken = System.getenv("GITHUB_TOKEN");

        //깃허브 크롤링 api 호출
        crawlOnlyFolderMarkdowns(repoInfo.getOwner(), repoInfo.getRepo(), repoInfo.getPath());
    }

    private void crawlOnlyFolderMarkdowns(String owner, String repo, String path) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken); // Optional
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {}
        );

        for (Map<String, Object> item : response.getBody()) {
            String type = (String) item.get("type");
            String name = (String) item.get("name");
            String filePath = (String) item.get("path");

            //폴더면 재귀 호출
            if ("dir".equals(type)) {
                crawlOnlyFolderMarkdowns(owner, repo, filePath);
            }

            // 2. 폴더 안의 md 파일만 처리
            else if ("file".equals(type) && name.endsWith(".md") && filePath.contains("/")) {
                String downloadUrl = (String) item.get("download_url");
                String content = restTemplate.getForObject(downloadUrl, String.class);
                saveMarkdown(name, filePath, content);
            }
        }
    }

    private void saveMarkdown(String fileName, String path, String content){
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        metadata.put("path", path);
        metadata.put("source", "GitHub");

        Document doc = new Document(content, metadata);
        //일단 로컬에 저장 후, 저장이 잘 되면 RagService 호출로 리팩터링
        saveToFile(doc);
    }

    private void saveToFile(Document document) {

        String SAVE_DIR = "data/markdowns";
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));

            String safeFileName = document.getMetadata().get("path").toString().replace("/", "-").replace(".md", ".txt");
            Path filePath = Paths.get(SAVE_DIR, safeFileName);

            // 파일 쓰기
            Files.writeString(filePath, document.getText(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.err.println("저장 실패: " + e.getMessage());
        }
    }
}
