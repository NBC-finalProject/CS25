package com.example.cs25.global.crawler.service;

import com.example.cs25.domain.ai.service.RagService;
import com.example.cs25.global.crawler.github.GitHubRepoInfo;
import com.example.cs25.global.crawler.github.GitHubUrlParser;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final RagService ragService;
    private final RestTemplate restTemplate;
    private String githubToken;

    public void crawlingGithubDocument(String url) {
        //url 에서 필요 정보 추출
        GitHubRepoInfo repoInfo = GitHubUrlParser.parseGitHubUrl(url);

        githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken == null || githubToken.trim().isEmpty()) {
            throw new IllegalStateException("GITHUB_TOKEN 환경변수가 설정되지 않았습니다.");
        }
        //깃허브 크롤링 api 호출
        List<Document> documentList = crawlOnlyFolderMarkdowns(repoInfo.getOwner(),
            repoInfo.getRepo(), repoInfo.getPath());

        //List 에 저장된 문서 ChromaVectorDB에 저장
        //ragService.saveDocumentsToVectorStore(documentList);
        saveToFile(documentList);
    }

    private List<Document> crawlOnlyFolderMarkdowns(String owner, String repo, String path) {
        List<Document> docs = new ArrayList<>();

        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken); // Optional
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<>() {
            }
        );

        for (Map<String, Object> item : response.getBody()) {
            String type = (String) item.get("type");
            String name = (String) item.get("name");
            String filePath = (String) item.get("path");

            //폴더면 재귀 호출
            if ("dir".equals(type)) {
                List<Document> subDocs = crawlOnlyFolderMarkdowns(owner, repo, filePath);
                docs.addAll(subDocs);
            }

            // 2. 폴더 안의 md 파일만 처리
            else if ("file".equals(type) && name.endsWith(".md") && filePath.contains("/")) {
                String downloadUrl = (String) item.get("download_url");
                downloadUrl = URLDecoder.decode(downloadUrl, StandardCharsets.UTF_8);
                //System.out.println("DOWNLOAD URL: " + downloadUrl);
                try {
                    String content = restTemplate.getForObject(downloadUrl, String.class);
                    Document doc = makeDocument(name, filePath, content);
                    docs.add(doc);
                } catch (HttpClientErrorException e) {
                    System.err.println(
                        "다운로드 실패: " + downloadUrl + " → " + e.getStatusCode());
                } catch (Exception e) {
                    System.err.println("예외: " + downloadUrl + " → " + e.getMessage());
                }
            }
        }

        return docs;
    }

    private Document makeDocument(String fileName, String path, String content) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", fileName);
        metadata.put("path", path);
        metadata.put("source", "GitHub");

        return new Document(content, metadata);
    }

    private void saveToFile(List<Document> docs) {
        String SAVE_DIR = "data/markdowns";

        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.err.println("디렉토리 생성 실패: " + e.getMessage());
            return;
        }

        for (Document document : docs) {
            try {
                String safeFileName = document.getMetadata().get("path").toString()
                    .replace("/", "-")
                    .replace(".md", ".txt");
                Path filePath = Paths.get(SAVE_DIR, safeFileName);

                Files.writeString(filePath, document.getText(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println(
                    "파일 저장 실패 (" + document.getMetadata().get("path") + "): " + e.getMessage());
            }
        }
    }
}