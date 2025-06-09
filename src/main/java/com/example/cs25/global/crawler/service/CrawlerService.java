package com.example.cs25.global.crawler.service;

import com.example.cs25.domain.ai.service.RagService;
import com.example.cs25.global.crawler.github.GitHubRepoInfo;
import com.example.cs25.global.crawler.github.GitHubUrlParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final RagService ragService;
    private final RestTemplate restTemplate;
    private String githubToken;

    public void crawlingGithubDocument(String url) {
        GitHubRepoInfo repoInfo = GitHubUrlParser.parseGitHubUrl(url);

        githubToken = System.getenv("GITHUB_TOKEN");
        if (githubToken == null || githubToken.trim().isEmpty()) {
            throw new IllegalStateException("GITHUB_TOKEN 환경변수가 설정되지 않았습니다.");
        }

        List<Document> documentList = crawlOnlyFolderMarkdowns(repoInfo.getOwner(),
                repoInfo.getRepo(), repoInfo.getPath());

        // 크롤링 완료 후, 문서 리스트 로그 추가
        log.info("크롤링 완료, 문서 개수: {}", documentList.size());
        for (Document doc : documentList) {
            log.info("문서 경로: {}, 글자 수: {}", doc.getMetadata().get("path"), doc.getText().length());
            log.info("문서 내용(앞 200자): {}", doc.getText().substring(0, Math.min(doc.getText().length(), 200)));
        }

        // 벡터스토어에 저장 시 에러 스택 트레이스도 로그로 남기기
        try {
            ragService.saveDocumentsToVectorStore(documentList);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.error("에러 발생: {}", e.getMessage());
            log.error("전체 스택 트레이스:\n{}", stackTrace);
        }

        // 파일로 저장 (테스트용)
        saveToFile(documentList);
    }

    private List<Document> crawlOnlyFolderMarkdowns(String owner, String repo, String path) {
        List<Document> docs = new ArrayList<>();

        // 직접 경로 조합 시 인코딩 적용
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + encodePath(path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
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

            if ("dir".equals(type)) {
                List<Document> subDocs = crawlOnlyFolderMarkdowns(owner, repo, filePath);
                docs.addAll(subDocs);
            }
            else if ("file".equals(type) && name.endsWith(".md") && filePath.contains("/")) {
                String downloadUrl = (String) item.get("download_url");
                if (downloadUrl == null) {
                    log.warn("download_url이 null인 파일: {}", filePath);
                    continue;
                }
                // download_url은 그대로 사용 (추가 인코딩 X)
                try {
                    String content = restTemplate.getForObject(downloadUrl, String.class);
                    if (content != null && !content.trim().isEmpty()) {
                        Document doc = makeDocument(name, filePath, content);
                        docs.add(doc);
                        log.info("정상적으로 다운로드: {}", filePath);
                    } else {
                        log.warn("빈 내용의 파일: {}", filePath);
                    }
                } catch (HttpClientErrorException e) {
                    log.error("다운로드 실패: {} → {}", downloadUrl, e.getStatusCode());
                } catch (Exception e) {
                    log.error("예외: {} → {}", downloadUrl, e.getMessage());
                }
            }
        }
        return docs;
    }

    // 직접 경로 조합 시에만 인코딩 적용
    private String encodePath(String path) {
        // 이미 인코딩된 %20 등이 있으면 decode해서 원본으로 만듦
        String decodedPath = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        // 다시 한 번만 인코딩
        String[] parts = decodedPath.split("/");
        StringBuilder encodedPath = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String encodedPart = URLEncoder.encode(parts[i], StandardCharsets.UTF_8);
            encodedPart = encodedPart.replace("+", "%20");
            encodedPath.append(encodedPart);
            if (i < parts.length - 1) {
                encodedPath.append("/");
            }
        }
        return encodedPath.toString();
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
            log.error("디렉토리 생성 실패: {}", e.getMessage());
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
                log.error("파일 저장 실패 ({}): {}", document.getMetadata().get("path"), e.getMessage());
            }
        }
    }
}
