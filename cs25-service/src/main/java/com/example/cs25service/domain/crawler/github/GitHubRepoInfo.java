package com.example.cs25service.domain.crawler.github;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GitHubRepoInfo {

    private final String owner;
    private final String repo;
    private final String path;

    @Override
    public String toString() {
        return "owner: " + owner + ", repo: " + repo + ", path: " + path;
    }
}
