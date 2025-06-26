package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizAccuracy;
import com.example.cs25entity.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizAccuracyCalculateService {

    private final QuizRepository quizRepository;
    private final QuizAccuracyRedisRepository quizAccuracyRedisRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;


    public void calculateAndCacheAllQuizAccuracies() {
        List<Quiz> quizzes = quizRepository.findAll();

        List<QuizAccuracy> accuracyList = new ArrayList<>();
        for (Quiz quiz : quizzes) {

            List<UserQuizAnswer> answers = userQuizAnswerRepository.findAllByQuizId(quiz.getId());
            long total = answers.size();
            long correct = answers.stream().filter(UserQuizAnswer::getIsCorrect).count();
            double accuracy = total == 0 ? 100.0 : ((double) correct / total) * 100.0;

            QuizAccuracy qa = QuizAccuracy.builder()
                .id("quiz:" + quiz.getId())
                .quizId(quiz.getId())
                .categoryId(quiz.getCategory().getId())
                .accuracy(accuracy)
                .build();

            accuracyList.add(qa);
        }
        log.info("총 {}개의 정답률 캐싱 완료", accuracyList.size());
        quizAccuracyRedisRepository.saveAll(accuracyList);
    }

}
