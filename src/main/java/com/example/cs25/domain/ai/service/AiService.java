package com.example.cs25.domain.ai.service;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.exception.AiException;
import com.example.cs25.domain.ai.exception.AiExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final RagService ragService;

    public AiFeedbackResponse getFeedback(Long quizId, Long subscriptionId) {

        var quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_QUIZ));

        var answer = userQuizAnswerRepository.findFirstByQuizIdAndSubscriptionIdOrderByCreatedAtDesc(
                quizId, subscriptionId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        StringBuilder context = new StringBuilder();
        List<Document>  relevantDocs = ragService.searchRelevant(quiz.getQuestion());
        for (Document doc : relevantDocs) {
            context.append("- 문서: ").append(doc.getText()).append("\n");
        }

        String prompt = """
            당신은 CS 문제 채점 전문가입니다. 아래 문서를 참고하여 사용자의 답변이 문제의 요구사항에 부합하는지 판단하세요.
            문서가 충분하지 않거나 관련 정보가 없는 경우, 당신이 알고 있는 CS 지식으로 보완해서 판단해도 됩니다.

            문서:
            %s

            문제: %s
            사용자 답변: %s

            아래 형식으로 답변하세요:
            - 정답 또는 오답: 이유를 명확하게 작성
            - 피드백: 어떤 점이 잘되었고, 어떤 점을 개선해야 하는지 구체적으로 작성
            """.formatted(context, quiz.getQuestion(), answer.getUserAnswer());


        String feedback;
        try {
            feedback = chatClient.prompt()
                .system("너는 CS 지식을 평가하는 채점관이야. 문제와 답변을 보고 '정답' 또는 '오답'으로 시작하는 문장으로 답변해. " +
                    "다른 단어나 표현은 사용하지 말고, 반드시 '정답' 또는 '오답'으로 시작해. " +
                    "그리고 사용자 답변에 대한 피드백도 반드시 작성해.")
                .user(prompt)
                .call()
                .content();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }

        boolean isCorrect = feedback.trim().startsWith("정답");

        answer.updateIsCorrect(isCorrect);
        answer.updateAiFeedback(feedback);
        userQuizAnswerRepository.save(answer);

        return new AiFeedbackResponse(
            quiz.getId(),
            isCorrect,
            feedback,
            answer.getId()
        );
    }
}
