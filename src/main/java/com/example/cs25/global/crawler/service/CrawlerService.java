package com.example.cs25.global.crawler.service;

import com.example.cs25.global.crawler.github.GitHubRepoInfo;
import com.example.cs25.global.crawler.github.GitHubUrlParser;

public class CrawlerService {
    public void crawlingGithubDocument(String url){
        //url에서 필요 정보 추출
        GitHubRepoInfo repoInfo = GitHubUrlParser.parseGitHubUrl(url);
    }
}
