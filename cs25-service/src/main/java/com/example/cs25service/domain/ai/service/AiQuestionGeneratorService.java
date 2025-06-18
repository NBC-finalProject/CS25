package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.entity.QuizFormatType;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiQuestionGeneratorService {

    private final ChatClient chatClient;
    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizCategoryRepository;
    private final RagService ragService;
    private final AiPromptProvider promptProvider;

    @Transactional
    public Quiz generateQuestionFromContext() {
        // 1. GPT에게 랜덤 키워드 요청
        String keyword = chatClient.prompt()
            .system(promptProvider.getRandomKeywordSystem())
            .user(promptProvider.getRandomKeywordUser())
            .call()
            .content()
            .trim();

        // 2. 해당 키워드로 RAG 검색
        List<Document> docs = ragService.searchRelevant(keyword, 3, 0.1);
        if (docs.isEmpty()) {
            throw new IllegalStateException("RAG 검색 결과가 없습니다.");
        }

        String context = docs.stream()
            .map(doc -> "- 문서 내용: " + doc.getText())
            .collect(Collectors.joining("\n"));

        if (!StringUtils.hasText(context)) {
            throw new IllegalStateException("RAG로부터 가져온 문서가 비어 있습니다.");
        }

        // 3. 중심 주제 추출
        String topic = chatClient.prompt()
            .system(promptProvider.getTopicSystem())
            .user(promptProvider.getTopicUser(context))
            .call()
            .content()
            .trim();

        // 4. 카테고리 분류
        String categoryType = chatClient.prompt()
            .system(promptProvider.getCategorySystem())
            .user(promptProvider.getCategoryUser(topic))
            .call()
            .content()
            .trim()
            .toUpperCase();

        if (!categoryType.equals("BACKEND") && !categoryType.equals("FRONTEND")) {
            throw new IllegalArgumentException("AI가 반환한 카테고리가 유효하지 않습니다: " + categoryType);
        }

        QuizCategory category = quizCategoryRepository.findByCategoryTypeOrElseThrow(categoryType);

        // 5. 문제 생성
        String output = chatClient.prompt()
            .system(promptProvider.getGenerateSystem())
            .user(promptProvider.getGenerateUser(context))
            .call()
            .content()
            .trim();

        String[] lines = output.split("\n");
        String question = extractField(lines, "문제:");
        String answer = extractField(lines, "정답:");
        String commentary = extractField(lines, "해설:");

        Quiz quiz = Quiz.builder()
            .type(QuizFormatType.SUBJECTIVE)
            .question(question)
            .answer(answer)
            .commentary(commentary)
            .category(category)
            .build();

        return quizRepository.save(quiz);
    }

    private String extractField(String[] lines, String prefix) {
        for (String line : lines) {
            if (line.trim().startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }
}
