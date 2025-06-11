package com.example.cs25.domain.ai.service;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiQuestionGeneratorService {

    private final ChatClient chatClient;
    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizCategoryRepository;
    private final RagService ragService;


    @Transactional
    public Quiz generateQuestionFromContext() {
        // Step 1. RAG 기반 문서 자동 선택
        List<Document> relevantDocs = ragService.searchRelevant("컴퓨터 과학 일반"); // 넓은 범위의 키워드로 시작

        // Step 2. 문서 context 구성
        StringBuilder context = new StringBuilder();
        for (Document doc : relevantDocs) {
            context.append("- 문서 내용: ").append(doc.getText()).append("\n");
        }

        // Step 3. 주제 자동 추출
        String topicExtractionPrompt = """
            아래 문서들을 읽고 중심 주제를 하나만 뽑아 한 문장으로 요약해줘.
            예시는 다음과 같아: 캐시 메모리, 트랜잭션 격리 수준, RSA 암호화, DNS 구조 등.
            반드시 핵심 개념 하나만 출력할 것.
            
            문서 내용:
            %s
            """.formatted(context);

        String extractedTopic = chatClient.prompt()
                .system("너는 문서에서 중심 주제를 추출하는 CS 요약 전문가야. 반드시 하나의 키워드만 출력해.")
                .user(topicExtractionPrompt)
                .call()
                .content()
                .trim();

        // Step 4. 카테고리 자동 분류
        String categoryPrompt = """
            다음 주제를 아래 카테고리 중 하나로 분류하세요: 운영체제, 컴퓨터구조, 자료구조, 네트워크, DB, 보안
            주제: %s
            결과는 카테고리 이름만 출력하세요.
            """.formatted(extractedTopic);

        String categoryType = chatClient.prompt()
                .system("너는 CS 주제를 기반으로 카테고리를 자동 분류하는 전문가야. 하나만 출력해.")
                .user(categoryPrompt)
                .call()
                .content()
                .trim();

        QuizCategory category = quizCategoryRepository.findByCategoryTypeOrElseThrow(categoryType);

        // Step 5. 문제 생성
        String generationPrompt = """
            너는 컴퓨터공학 시험 출제 전문가야.
            아래 문서를 기반으로 주관식 문제, 모범답안, 해설을 생성해.
            
            [조건]
            1. 문제는 하나의 문장으로 명확하게 작성
            2. 정답은 핵심 개념을 포함한 모범답안
            3. 해설은 정답의 근거를 문서 기반으로 논리적으로 작성
            4. 출력 형식:
            문제: ...
            정답: ...
            해설: ...
            
            문서 내용:
            %s
            """.formatted(context);

        String aiOutput = chatClient.prompt()
                .system("너는 문서 기반으로 문제를 출제하는 전문가야. 정확히 문제/정답/해설 세 부분을 출력해.")
                .user(generationPrompt)
                .call()
                .content()
                .trim();

        // Step 6. Parsing
        String[] lines = aiOutput.split("\n");
        String question = extractField(lines, "문제:");
        String answer = extractField(lines, "정답:");
        String commentary = extractField(lines, "해설:");

        // Step 7. 저장
        Quiz quiz = Quiz.builder()
                .type(QuizFormatType.SUBJECTIVE)
                .question(question)
                .answer(answer)
                .commentary(commentary)
                .category(category)
                .build();

        return quizRepository.save(quiz);
    }


    public static String extractField(String[] lines, String prefix) {
        for (String line : lines) {
            if (line.trim().startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

}
