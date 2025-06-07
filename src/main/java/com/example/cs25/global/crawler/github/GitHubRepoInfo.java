package com.example.cs25.global.crawler.github;

import lombok.AllArgsConstructor;

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
