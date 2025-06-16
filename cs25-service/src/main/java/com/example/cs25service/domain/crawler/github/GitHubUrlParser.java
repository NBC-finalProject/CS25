package com.example.cs25service.domain.crawler.github;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitHubUrlParser {

    public static GitHubRepoInfo parseGitHubUrl(String url) {
        // 정규식 보완: /tree/, /blob/, /main/, /master/ 등 다양한 패턴 지원
        String regex = "^https://github\\.com/([^/]+)/([^/]+)(/(?:tree|blob|main|master)/[^/]+(/.+))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            String path = matcher.group(4);
            if (path != null && path.startsWith("/")) {
                path = path.substring(1); // remove leading '/'
                // path에 %가 포함되어 있으면 이미 인코딩된 값으로 간주, decode
                if (path.contains("%")) {
                    try {
                        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        log.warn("decode 실패: {}", path);
                    }
                }
            }
            log.info("입력 URL: {}", url);
            log.info("owner: {}, repo: {}, path: {}", owner, repo, path);
            return new GitHubRepoInfo(owner, repo, path != null ? path : "");
        } else {
            throw new IllegalArgumentException("유효하지 않은 Github Repository 주소입니다.");
        }
    }
}
