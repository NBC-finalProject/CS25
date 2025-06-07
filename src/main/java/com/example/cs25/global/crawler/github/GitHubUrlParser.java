package com.example.cs25.global.crawler.github;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubUrlParser {
    public static GitHubRepoInfo parseGitHubUrl(String url) {
        String regex = "^https://github\\.com/([^/]+)/([^/]+)(/tree/[^/]+(/.+))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            String path = matcher.group(4);
            if (path != null && path.startsWith("/")) {
                path = path.substring(1); // remove leading '/'
            }
            return new GitHubRepoInfo(owner, repo, path != null ? path : "");
        } else {
            throw new IllegalArgumentException("유효하지 않은 Github Repository 주소입니다.");
        }
    }
}
