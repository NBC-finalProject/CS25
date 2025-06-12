package com.example.cs25.domain.ai.prompt;

import com.example.cs25.domain.ai.config.AiPromptProperties;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiPromptProvider {

    private final AiPromptProperties props;

    // === [Feedback] ===
    public String getFeedbackSystem() {
        return props.getFeedback().getSystem();
    }

    public String getFeedbackUser(Quiz quiz, UserQuizAnswer answer, List<Document> docs) {
        String context = docs.stream()
            .map(doc -> "- 문서: " + doc.getText())
            .collect(Collectors.joining("\n"));

        return props.getFeedback().getUser()
            .replace("{context}", context)
            .replace("{question}", quiz.getQuestion())
            .replace("{userAnswer}", answer.getUserAnswer());
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
