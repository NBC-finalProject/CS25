package com.example.cs25.global.crawler.service;

import com.example.cs25.domain.ai.service.RagService;
import com.example.cs25.global.crawler.github.GitHubRepoInfo;
import com.example.cs25.global.crawler.github.GitHubUrlParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
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
        log.info("크롤링 시작: {}", url);
        try {
            GitHubRepoInfo repoInfo = GitHubUrlParser.parseGitHubUrl(url);
            log.info("파싱된 정보: owner={}, repo={}, path={}",
                    repoInfo.getOwner(), repoInfo.getRepo(), repoInfo.getPath());

            githubToken = System.getenv("GITHUB_TOKEN");
            if (githubToken == null || githubToken.trim().isEmpty()) {
                log.error("GITHUB_TOKEN 환경변수가 설정되지 않았습니다.");
                throw new IllegalStateException("GITHUB_TOKEN 환경변수가 설정되지 않았습니다.");
            } else {
                log.info("GITHUB_TOKEN 확인: {}", githubToken.substring(0, 4) + "...");
            }

            List<Document> documentList = crawlOnlyFolderMarkdowns(repoInfo.getOwner(),
                    repoInfo.getRepo(), repoInfo.getPath());

            // 문서를 5000자 단위로 분할
            List<Document> splitDocs = new ArrayList<>();
            for (Document doc : documentList) {
                splitDocs.addAll(splitDocument(doc, 5000));
            }

            log.info("크롤링 완료, 분할된 문서 개수: {}", splitDocs.size());
            for (Document doc : splitDocs) {
                log.info("문서 경로: {}, 글자 수: {}", doc.getMetadata().get("path"), doc.getText().length());
                log.info("문서 내용(앞 100자): {}", doc.getText().substring(0, Math.min(doc.getText().length(), 100)));
            }

            try {
                ragService.saveDocumentsToVectorStore(splitDocs);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                log.error("벡터스토어 저장 중 에러 발생: {}", e.getMessage());
                log.error("전체 스택 트레이스:\n{}", stackTrace);
            }

            saveToFile(splitDocs);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.error("크롤링 중 예외 발생: {}", e.getMessage());
            log.error("전체 스택 트레이스:\n{}", stackTrace);
        }
    }

    private List<Document> crawlOnlyFolderMarkdowns(String owner, String repo, String path) {
        List<Document> docs = new ArrayList<>();
        try {
            // 직접 경로 조합 시 인코딩 적용
            String encodedPath = encodePath(path);
            log.info("인코딩 전 경로: {}", path);
            log.info("인코딩 후 경로: {}", encodedPath);

            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + encodedPath;
            log.info("GitHub API 호출 URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);
            headers.set("User-Agent", "cs25-crawler");
            log.info("헤더: {}", headers);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            log.info("GitHub API 응답 상태: {}", response.getStatusCode());
            if (response.getBody() == null) {
                log.warn("GitHub API 응답 body가 null입니다.");
                return docs;
            }

            for (Map<String, Object> item : response.getBody()) {
                String type = (String) item.get("type");
                String name = (String) item.get("name");
                String filePath = (String) item.get("path");
                log.info("폴더/파일: type={}, name={}, path={}", type, name, filePath);

                if ("dir".equals(type)) {
                    List<Document> subDocs = crawlOnlyFolderMarkdowns(owner, repo, filePath);
                    docs.addAll(subDocs);
                } else if ("file".equals(type) && name.endsWith(".md") && filePath.contains("/")) {
                    String downloadUrl = (String) item.get("download_url");
                    if (downloadUrl == null) {
                        log.warn("download_url이 null인 파일: {}", filePath);
                        continue;
                    }
                    log.info("다운로드 URL: {}", downloadUrl);
                    try {
                        String content = restTemplate.getForObject(downloadUrl, String.class);
                        if (content != null && !content.trim().isEmpty()) {
                            Map<String, Object> metadata = new HashMap<>();
                            metadata.put("fileName", name);
                            metadata.put("path", filePath);
                            metadata.put("source", "GitHub");
                            docs.add(new Document(content, metadata));
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
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            log.error("GitHub API 호출 중 예외 발생: {}", e.getMessage());
            log.error("전체 스택 트레이스:\n{}", stackTrace);
        }

        return docs;
    }

    private List<Document> splitDocument(Document doc, int maxLength) {
        List<Document> result = new ArrayList<>();
        String text = doc.getText();
        Map<String, Object> metadata = new HashMap<>(doc.getMetadata());

        for (int i = 0; i < text.length(); i += maxLength) {
            int end = Math.min(i + maxLength, text.length());
            String subText = text.substring(i, end);
            result.add(new Document(subText, metadata));
        }
        return result;
    }

    private String encodePath(String path) {
        if (path.contains("%")) {
            try {
                String decodedPath = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
                log.info("decode 후 경로: {}", decodedPath);
                return encodeRawPath(decodedPath);
            } catch (Exception e) {
                log.warn("decode 실패: {}", path);
                return encodeRawPath(path);
            }
        } else {
            return encodeRawPath(path);
        }
    }

    private String encodeRawPath(String rawPath) {
        String[] parts = rawPath.split("/");
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
