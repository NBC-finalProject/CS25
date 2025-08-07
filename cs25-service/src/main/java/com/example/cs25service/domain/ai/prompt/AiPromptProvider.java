package com.example.cs25service.domain.ai.prompt;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25service.domain.ai.config.AiPromptProperties;
import com.example.cs25service.domain.ai.service.BraveSearchRagService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiPromptProvider {

    private final AiPromptProperties props;
    private final BraveSearchRagService braveSearchRagService;

    // === [Keyword] ===
    public String getKeywordSystem() {
        return props.getKeyword().getSystem();
    }

    public String getKeywordUser() {
        return props.getKeyword().getUser();
    }

    // === [Feedback] ===
    public String getFeedbackSystem() {
        return props.getFeedback().getSystem();
    }

    public String getFeedbackUser(Quiz quiz, UserQuizAnswer answer, List<Document> docs,
        Optional<JsonNode> braveResults) {
        String context = docs.stream()
            .map(doc -> "- 문서: " + doc.getText())
            .collect(Collectors.joining("\n"));

        String searchResults = braveResults
            .map(this::formatBraveResults)
            .orElse("");

        String userPrompt = props.getFeedback().getUser()
            .replace("{context}", context)
            .replace("{question}", quiz.getQuestion())
            .replace("{userAnswer}", answer.getUserAnswer())
            .replace("{searchResults}", searchResults);

        log.info("[AI User Prompt]\n{}", userPrompt); // 🔍 여기에 추가
        return userPrompt;

    }

    private String formatBraveResults(JsonNode root) {
        JsonNode resultsNode = root.get("results");
        if (resultsNode == null || !resultsNode.isArray()) {
            return "";
        }

        List<Document> docs = braveSearchRagService.toDocuments(Optional.of(root));

        return "[브레이브 검색 결과]\n" +
            docs.stream()
                .map(doc -> "- " + doc.getMetadata().get("title") + ": " + doc.getMetadata()
                    .get("url"))
                .collect(Collectors.joining("\n"));
    }

    // === [Generation] ===
    public String getTopicSystem() {
        return props.getGeneration().getTopicSystem();
    }

    public String getTopicUser(String context) {
        return props.getGeneration().getTopicUser()
            .replace("{context}", context);
    }

    public String getCategorySystem() {
        return props.getGeneration().getCategorySystem();
    }

    public String getCategoryUser(String topic) {
        return props.getGeneration().getCategoryUser()
            .replace("{topic}", topic);
    }

    public String getGenerateSystem() {
        return props.getGeneration().getGenerateSystem();
    }

    public String getGenerateUser(String context) {
        return props.getGeneration().getGenerateUser()
            .replace("{context}", context);
    }
}
