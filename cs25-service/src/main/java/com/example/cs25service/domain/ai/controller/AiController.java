package com.example.cs25service.domain.ai.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25service.domain.ai.service.AiFeedbackQueueService;
import com.example.cs25service.domain.ai.service.AiQuestionGeneratorService;
import com.example.cs25service.domain.ai.service.AiService;
import com.example.cs25service.domain.ai.service.FileLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiQuestionGeneratorService aiQuestionGeneratorService;
    private final FileLoaderService fileLoaderService;
    private final AiFeedbackQueueService aiFeedbackQueueService;

    @GetMapping("/answers/{answerId}/feedback-word")
    public SseEmitter streamWordFeedback(@PathVariable Long answerId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);

        aiFeedbackQueueService.enqueue(answerId, emitter, "word");
        return emitter;
    }

    @GetMapping("/answers/{answerId}/feedback-sentence")
    public SseEmitter streamSentenceFeedback(@PathVariable Long answerId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);

        aiFeedbackQueueService.enqueue(answerId, emitter, "sentence");
        return emitter;
    }

    @GetMapping("/generate")
    public ApiResponse<Quiz> generateQuiz() {
        Quiz quiz = aiQuestionGeneratorService.generateQuestionFromContext();
        return new ApiResponse<>(200, quiz);
    }

    @GetMapping("/load/{dirName}")
    public String loadFiles(@PathVariable("dirName") String dirName) {
        String basePath = "cs25-service/data/";
        fileLoaderService.loadAndSaveFiles(basePath + dirName);
        return "파일 적재 완료!";
    }


}